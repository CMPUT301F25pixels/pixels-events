package com.example.pixel_events.events;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.EventListViewHolder> {

    private final List<Event> events;
    private final OnEventClickListener clickListener;
    private final Context context;

    private static final SimpleDateFormat DATE_FORMAT = 
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsListAdapter(List<Event> events, OnEventClickListener clickListener) {
        this.events = events;
        this.clickListener = clickListener;
        this.context = null;
    }

    public EventsListAdapter(Context context, List<Event> events, OnEventClickListener clickListener) {
        this.context = context;
        this.events = events;
        this.clickListener = clickListener;
        loadEvents();
    }


    private void loadEvents() {
        if (context == null) return;

        DatabaseHandler.getInstance().getAllEvents(allEvents -> {
            if (allEvents != null) {
                List<Event> upcomingEvents = filterUpcomingEvents(allEvents);
                updateEvents(upcomingEvents);
            }
        });
    }

    
    private List<Event> filterUpcomingEvents(List<Event> allEvents) {
        List<Event> filtered = new ArrayList<>();
        Date today = new Date();

        for (Event event : allEvents) {
            try {
                Date eventDate = DATE_FORMAT.parse(event.getEventStartDate());
                if (eventDate.after(today) || eventDate.equals(today)) {
                    filtered.add(event);
                }
            } catch (ParseException e) {
                Log.e("EventsListAdapter", "Error parsing event date: " + e.getMessage());
            }
        }

        return filtered;
    }


    private List<Event> filterRegistrationOpenEvents(List<Event> allEvents) {
        List<Event> filtered = new ArrayList<>();
        Date today = new Date();

        for (Event event : allEvents) {
            try {
                Date regStart = DATE_FORMAT.parse(event.getRegistrationStartDate());
                Date regEnd = DATE_FORMAT.parse(event.getRegistrationEndDate());

    
                if ((today.after(regStart) || today.equals(regStart)) && today.before(regEnd)) {
                    filtered.add(event);
                } else if (today.before(regStart)) {
                    filtered.add(event);
                }
            } catch (ParseException e) {
                Log.e("EventsListAdapter", "Error parsing registration dates: " + e.getMessage());
            }
        }

        return filtered;
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);
        return new EventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, clickListener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
    }

   
    public void reloadUpcomingEvents() {
        DatabaseHandler.getInstance().getAllEvents(allEvents -> {
            if (allEvents != null) {
                List<Event> upcomingEvents = filterUpcomingEvents(allEvents);
                updateEvents(upcomingEvents);
            }
        });
    }

   
    public void reloadRegistrationOpenEvents() {
        DatabaseHandler.getInstance().getAllEvents(allEvents -> {
            if (allEvents != null) {
                List<Event> registrationEvents = filterRegistrationOpenEvents(allEvents);
                updateEvents(registrationEvents);
            }
        });
    }

    public static class EventListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final TextView eventTitle;
        private final TextView eventLocation;
        private final TextView eventTime;
        private final TextView eventStatus;

        private static final SimpleDateFormat TIME_FORMAT = 
                new SimpleDateFormat("h:mm a", Locale.US);
        private static final SimpleDateFormat DATE_FORMAT = 
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        public EventListViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventListImage);
            eventTitle = itemView.findViewById(R.id.eventListTitle);
            eventLocation = itemView.findViewById(R.id.eventListLocation);
            eventTime = itemView.findViewById(R.id.eventListTime);
            eventStatus = itemView.findViewById(R.id.eventListStatus);
        }

        public void bind(Event event, OnEventClickListener listener) {
    
            if (event.getImageResId() > 0) {
                eventImage.setImageResource(event.getImageResId());
            }

        
            eventTitle.setText(event.getTitle() != null ? event.getTitle() : "");
            eventLocation.setText(event.getLocation() != null ? event.getLocation() : "");

         
            try {
                eventTime.setText(event.getFormattedTime() != null ? event.getFormattedTime() : "");
            } catch (Exception e) {
                eventTime.setText("");
            }

            String status = determineEventStatus(event);
            eventStatus.setText(status);

            int statusColor = getStatusColor(status, itemView.getContext());
            eventStatus.setTextColor(statusColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }

    
        private String determineEventStatus(Event event) {
            try {
                Date now = new Date();
                Date eventDate = DATE_FORMAT.parse(event.getEventStartDate());

                if (eventDate.after(now)) {
                    return "Upcoming";
                } else if (eventDate.equals(now)) {
                    return "Ongoing";
                } else {
                    return "Past";
                }
            } catch (ParseException e) {
                Log.e("EventListViewHolder", "Error parsing event date: " + e.getMessage());
                return "Unknown";
            }
        }

        
        private int getStatusColor(String status, Context context) {
            int colorRes;
            switch (status) {
                case "Upcoming":
                    colorRes = R.color.status_upcoming;
                    break;
                case "Ongoing":
                    colorRes = R.color.status_ongoing;
                    break;
                case "Past":
                    colorRes = R.color.status_past;
                    break;
                default:
                    colorRes = R.color.status_not_selected;
            }
            return context.getColor(colorRes);
        }
    }
}