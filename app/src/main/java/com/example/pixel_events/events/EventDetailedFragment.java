package com.example.pixel_events.events;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pixel_events.R;
import com.example.pixel_events.database.DatabaseHandler;
import com.example.pixel_events.login.AuthManager;
import com.example.pixel_events.waitinglist.WaitingList;
import com.example.pixel_events.waitinglist.WaitlistUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import android.graphics.Bitmap;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Objects;

import com.example.pixel_events.utils.ImageConversion;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailedFragment extends Fragment {
    private static final String TAG = "EventDetailedFragment";
    private Event event;
    private WaitingList waitList;
    private DatabaseHandler db;
    private int userId; // current profile id
    private int eventId = -1;
    private boolean joined = false;
    private int waitingListCount = 0, waitingListMaxCount = 0;

    // UI elements
    private ShapeableImageView poster;
    private TextView title, date, description;
    private MaterialButton joinButton, leaveButton, tagButton;
    private TextView waitingListCountView;
    private ImageButton backButton, qrButton;
    private LinearLayout tagsContainer;

    public EventDetailedFragment() {
    }

    public EventDetailedFragment(int eventId) {
        this.eventId = eventId;
    }

    public EventDetailedFragment(Event event) {
        this.event = event;
        this.eventId = event.getEventId();
    }

    public EventDetailedFragment(Event event, WaitingList waitingList) {
        this.event = event;
        this.waitList = waitingList;
        this.eventId = event.getEventId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detailed, container, false);

        db = DatabaseHandler.getInstance();

        userId = -1;
        eventId = -1;

        // Check for Test Arguments (Backdoor)
        if (getArguments() != null) {
            if (getArguments().containsKey("userId")) {
                userId = getArguments().getInt("userId");
            }
            if (getArguments().containsKey("eventId")) {
                eventId = getArguments().getInt("eventId");
            }
        }

        // If Test Arguments didn't provide a User, try AuthManager (Production)
        if (userId == -1 && AuthManager.getInstance().getCurrentUserProfile() != null) {
            userId = AuthManager.getInstance().getCurrentUserProfile().getUserId();
        }

        // Initialize UI
        poster = view.findViewById(R.id.event_poster);
        title = view.findViewById(R.id.event_title);
        date = view.findViewById(R.id.event_date);
        description = view.findViewById(R.id.event_description);
        joinButton = view.findViewById(R.id.event_joinButton);
        leaveButton = view.findViewById(R.id.event_leaveButton);
        backButton = view.findViewById(R.id.event_backbutton);
        tagButton = view.findViewById(R.id.event_tag);
        qrButton = view.findViewById(R.id.event_qrcode_button);
        tagsContainer = view.findViewById(R.id.eventTagsContainer);
        // Waiting list count view (under the buttons)
        waitingListCountView = view.findViewById(R.id.event_waitinglistcount);
        if (waitingListCountView != null)
            waitingListCountView.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        qrButton.setOnClickListener(v -> showQrDialog());

        // Start with tag hidden
        tagButton.setVisibility(GONE);

        // Load event data
        if (eventId >= 0) {
            if (event == null) {
                db.getEvent(eventId,
                        evt -> {
                            event = evt;
                            if (isAdded())
                                requireActivity().runOnUiThread(this::updateUI);
                            if (isAdded()) {
                                requireActivity().runOnUiThread(this::renderCTA);
                            }
                        },
                        error -> {
                            Log.e(TAG, "Error getting event: " + error);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
            } else {
                updateUI();
            }

            if (waitList == null) {
                // Load waitlist state
                db.getWaitingList(eventId, wl -> {
                    if (wl != null) {
                        this.waitList = wl;
                        List<WaitlistUser> ids = wl.getWaitList();
                        if (ids == null)
                            ids = java.util.Collections.emptyList();
                        joined = false;
                        for (WaitlistUser user : ids) {
                            if (user.getUserId() == userId) {
                                joined = true;
                                break;
                            }
                        }
                        waitingListCount = ids.size();
                        waitingListMaxCount = wl.getMaxWaitlistSize();
                    } else {
                        joined = false;
                        waitingListCount = 0;
                    }
                    if (isAdded())
                        requireActivity().runOnUiThread(this::renderCTA);
                }, e -> {
                    Log.e(TAG, "Failed to fetch waitlist", e);
                    joined = false;
                    waitingListCount = 0;
                    if (isAdded())
                        requireActivity().runOnUiThread(this::renderCTA);
                });
            } else {
                List<WaitlistUser> ids = waitList.getWaitList();
                if (ids == null)
                    ids = java.util.Collections.emptyList();
                joined = false;
                for (WaitlistUser user : ids) {
                    if (user.getUserId() == userId) {
                        joined = true;
                        break;
                    }
                }
                waitingListCount = ids.size();
                waitingListMaxCount = waitList.getMaxWaitlistSize();
                if (isAdded())
                    requireActivity().runOnUiThread(this::renderCTA);
            }
        }

        // Wire separate handlers for Join and Leave
        joinButton.setOnClickListener(v -> {
            if (userId == -1) {
                toast("You must be logged in to join the waitlist");
                return;
            }
            setButtonEnabled(joinButton, false);
            setButtonEnabled(leaveButton, false);
            joinWaitlist();
        });

        leaveButton.setOnClickListener(v -> {
            if (userId == -1) {
                toast("You must be logged in to leave the waitlist");
                return;
            }
            setButtonEnabled(leaveButton, false);
            setButtonEnabled(joinButton, false);
            leaveWaitlist();
        });

        return view;
    }

    private void updateUI() {
        if (!isAdded() || event == null)
            return;

        title.setText(event.getTitle());
        date.setText(event.getDateString());
        description.setText(event.getFullDescription());

        // Load poster image
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Bitmap bitmap = ImageConversion.base64ToBitmap(event.getImageUrl());
            if (bitmap != null)
                poster.setImageBitmap(bitmap);
        }

        displayTags();
    }

    private void displayTags() {
        if (tagsContainer == null || event == null)
            return;

        tagsContainer.removeAllViews();
        List<String> tags = event.getTags();
        if (tags == null || tags.isEmpty())
            return;

        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty())
                continue;
            TextView tagView = new TextView(requireContext());
            tagView.setText(tag);
            tagView.setTextColor(getResources().getColor(R.color.white, null));
            tagView.setTextSize(12);
            tagView.setPadding(16, 8, 16, 8);
            tagView.setBackground(getResources().getDrawable(R.drawable.view_tag_outline, null));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            tagView.setLayoutParams(params);
            tagsContainer.addView(tagView);
        }
    }

    private void renderCTA() {
        if (!isAdded() || joinButton == null || leaveButton == null || tagButton == null)
            return;

        // If event not yet loaded, show loading state
        if (event == null) {
            tagButton.setVisibility(GONE);
            joinButton.setText("Loading registration…");
            setButtonEnabled(joinButton, false);
            setButtonEnabled(leaveButton, false);
            if (waitingListCountView != null)
                waitingListCountView.setVisibility(View.GONE);
            return;
        }

        // Determine registration window
        String startStr = event.getRegistrationStartDate();
        String endStr = event.getRegistrationEndDate();
        if (startStr != null)
            startStr = startStr.trim();
        if (endStr != null)
            endStr = endStr.trim();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date today = truncateToDay(new Date());
            Date start = (startStr != null && !startStr.isEmpty()) ? truncateToDay(fmt.parse(startStr)) : null;
            Date end = (endStr != null && !endStr.isEmpty()) ? truncateToDay(fmt.parse(endStr)) : null;

            if (start == null || end == null) {
                tagButton.setVisibility(View.VISIBLE);
                joinButton.setVisibility(GONE);
                leaveButton.setVisibility(GONE);
                tagButton.setText("Registration dates missing");
                setButtonEnabled(tagButton, false);
                if (waitingListCountView != null)
                    waitingListCountView.setVisibility(View.GONE);
                return;
            }

            if (today.before(start)) {
                tagButton.setVisibility(View.VISIBLE);
                joinButton.setVisibility(GONE);
                leaveButton.setVisibility(GONE);
                tagButton.setText("Registration opens " + startStr);
                setButtonEnabled(tagButton, false);
                if (waitingListCountView != null)
                    waitingListCountView.setVisibility(View.GONE);
                return;
            }
            if (today.after(end)) {
                tagButton.setVisibility(View.VISIBLE);
                joinButton.setVisibility(GONE);
                leaveButton.setVisibility(GONE);
                tagButton.setText("Registration Closed");
                setButtonEnabled(tagButton, false);
                if (waitingListCountView != null)
                    waitingListCountView.setVisibility(View.GONE);
                return;
            }

            // Lottery drawn? handle accept/decline/final states
            tagButton.setVisibility(GONE);
            joinButton.setVisibility(View.VISIBLE);
            leaveButton.setVisibility(View.VISIBLE);
            if (waitingListCountView != null) {
                waitingListCountView.setVisibility(View.VISIBLE);
                waitingListCountView.setText(String.valueOf(waitingListCount) + " in waiting list");
            }

            if (this.waitList != null && Objects.equals(this.waitList.getStatus(), "drawn")) {
                int userStatus = -1;
                boolean wasInWaitlist = false;
                if (this.waitList.getWaitList() != null) {
                    for (WaitlistUser user : this.waitList.getWaitList()) {
                        if (user.getUserId() == userId) {
                            userStatus = user.getStatus();
                            wasInWaitlist = true;
                            break;
                        }
                    }
                }

                if (userStatus == 1) {
                    // selected -> show Accept (join) and Decline (leave)
                    joinButton.setText("Accept Invitation");
                    setButtonEnabled(joinButton, true);
                    joinButton.setOnClickListener(v -> {
                        setButtonEnabled(joinButton, false);
                        setButtonEnabled(leaveButton, false);
                        updateUserStatus(2);
                    });

                    leaveButton.setText("Decline Invitation");
                    setButtonEnabled(leaveButton, true);
                    leaveButton.setOnClickListener(v -> {
                        setButtonEnabled(leaveButton, false);
                        setButtonEnabled(joinButton, false);
                        updateUserStatus(3);
                        // redraw should be triggered after status update and reload; keep here for
                        // safety
                        waitList.drawLottery(new WaitingList.OnLotteryDrawnListener() {
                            @Override
                            public void onSuccess(int numberDrawn) {
                                Log.d(TAG, "Lottery drawn: " + numberDrawn);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to draw lottery", e);
                            }
                        });
                    });
                    return;
                }

                if (userStatus == 2) {
                    // accepted -> final tag
                    joinButton.setVisibility(GONE);
                    leaveButton.setVisibility(GONE);
                    tagButton.setVisibility(View.VISIBLE);
                    tagButton.setText("Invitation Accepted");
                    setButtonEnabled(tagButton, false);
                    return;
                }

                if (userStatus == 3) {
                    // declined -> final tag
                    joinButton.setVisibility(GONE);
                    leaveButton.setVisibility(GONE);
                    tagButton.setVisibility(View.VISIBLE);
                    tagButton.setText("Invitation declined");
                    setButtonEnabled(tagButton, false);
                    return;
                }

                if (userStatus == 0 && today.after(end)) {
                    joinButton.setVisibility(GONE);
                    leaveButton.setVisibility(GONE);
                    tagButton.setVisibility(View.VISIBLE);
                    tagButton.setText("Sorry, you were not selected");
                    setButtonEnabled(tagButton, false);
                } else {
                    if (joined) {
                        joinButton.setText("Join");
                        setButtonEnabled(joinButton, false);

                        leaveButton.setText("Leave");
                        setButtonEnabled(leaveButton, true);
                    } else {
                        boolean canJoin = waitingListMaxCount <= 0 || waitingListCount < waitingListMaxCount;
                        if (!canJoin) {
                            joinButton.setVisibility(GONE);
                            leaveButton.setVisibility(GONE);
                            tagButton.setVisibility(View.VISIBLE);
                            tagButton.setText("Waitlist full");
                            setButtonEnabled(tagButton, false);
                            return;
                        }
                        joinButton.setText("Join");
                        setButtonEnabled(joinButton, true);

                        leaveButton.setText("Leave");
                        setButtonEnabled(leaveButton, false);
                    }
                    leaveButton.setOnClickListener(v -> {
                        waitList.drawLottery(new WaitingList.OnLotteryDrawnListener() {
                            @Override
                            public void onSuccess(int numberDrawn) {
                                Log.d(TAG, "Lottery drawn: " + numberDrawn);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to draw lottery", e);
                            }
                        });
                        joinButton.setVisibility(GONE);
                        leaveButton.setVisibility(GONE);
                        tagButton.setVisibility(View.VISIBLE);
                        tagButton.setText("Invitation declined");
                        setButtonEnabled(tagButton, false);
                    });
                }

            } else {
                // Not drawn: normal join/leave behavior
                if (joined) {
                    joinButton.setText("Join");
                    setButtonEnabled(joinButton, false);

                    leaveButton.setText("Leave");
                    setButtonEnabled(leaveButton, true);
                } else {
                    boolean canJoin = waitingListMaxCount <= 0 || waitingListCount < waitingListMaxCount;
                    if (!canJoin) {
                        joinButton.setVisibility(GONE);
                        leaveButton.setVisibility(GONE);
                        tagButton.setVisibility(View.VISIBLE);
                        tagButton.setText("Waitlist full");
                        setButtonEnabled(tagButton, false);
                        return;
                    }
                    joinButton.setText("Join");
                    setButtonEnabled(joinButton, true);

                    leaveButton.setText("Leave");
                    setButtonEnabled(leaveButton, false);
                }
            }

        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse registration dates. start='" + startStr + "' end='" + endStr + "'", e);
            tagButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(GONE);
            leaveButton.setVisibility(GONE);
            tagButton.setText("Registration dates invalid");
            setButtonEnabled(tagButton, false);
        }
    }

    private void joinWaitlist() {
        DatabaseHandler.getInstance()
                .joinWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> {
                    joined = true;
                    waitingListCount++;
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> {
                            toast("Joined waitlist");
                            renderCTA();
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Join failed", e);
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> {
                            toast("Error: " + e.getMessage());
                            setButtonEnabled(joinButton, true);
                            // leave stays disabled when not joined
                            setButtonEnabled(leaveButton, false);
                        });
                });
    }

    private void leaveWaitlist() {
        DatabaseHandler.getInstance()
                .leaveWaitingList(eventId, userId)
                .addOnSuccessListener(unused -> {
                    joined = false;
                    if (waitingListCount > 0)
                        waitingListCount--;
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> {
                            toast("Left waitlist");
                            renderCTA();
                        });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Leave failed", e);
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> {
                            toast("Error: " + e.getMessage());
                            setButtonEnabled(leaveButton, true);
                            setButtonEnabled(joinButton, false);
                        });
                });
    }

    private void setButtonEnabled(MaterialButton btn, boolean enabled) {
        if (btn == null)
            return;
        btn.setEnabled(enabled);
        // Visual cue for disabled state
        btn.setAlpha(enabled ? 1f : 0.5f);
    }

    private void updateUserStatus(int newStatus) {
        if (waitList == null)
            return;

        WaitlistUser currentUser = null;
        if (waitList.getWaitList() != null) {
            for (WaitlistUser user : waitList.getWaitList()) {
                if (user.getUserId() == userId) {
                    currentUser = user;
                    break;
                }
            }
        }

        if (currentUser != null) {
            currentUser.updateStatusInDb(eventId, newStatus, new WaitlistUser.OnStatusUpdateListener() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            toast(newStatus == 2 ? "Invitation accepted!" : "Invitation declined");
                            db.getWaitingList(eventId, wl -> {
                                waitList = wl;
                                renderCTA();
                            }, e -> Log.e(TAG, "Failed to reload waitlist", e));
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            toast("Failed to update invitation: " + e.getMessage());
                            Log.e(TAG, "Failed to update status", e);
                        });
                    }
                }
            });
        }
    }

    private void toast(String msg) {
        if (getContext() != null) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void showQrDialog() {
        if (!isAdded())
            return;
        if (event == null || event.getQrCode() == null || event.getQrCode().isEmpty()) {
            toast("QR code not available");
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = ImageConversion.base64ToBitmap(event.getQrCode());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid QR base64", e);
            toast("Failed to load QR code");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_qrcode, null, false);
        android.widget.ImageView imageView = dialogView.findViewById(R.id.qr_code_image);
        imageView.setImageBitmap(bitmap);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        android.widget.Button closeBtn = dialogView.findViewById(R.id.qr_dialog_close);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        } else {
            dialogView.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    private Date truncateToDay(Date d) {
        if (d == null)
            return null;
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            return dayFmt.parse(dayFmt.format(d));
        } catch (ParseException e) {
            return d; // fallback
        }
    }
}
