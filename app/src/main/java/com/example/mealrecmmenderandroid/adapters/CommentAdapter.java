package com.example.mealrecmmenderandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onReplyClick(Comment comment);
        void onViewRepliesClick(Comment comment);
    }

    public CommentAdapter(Context context, List<Comment> comments, OnCommentInteractionListener listener) {
        this.context = context;
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView commentTextView;
        TextView timestampTextView;
        RatingBar ratingBar;
        TextView replyButton;
        TextView viewRepliesButton;
        LinearLayout ratingContainer;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            replyButton = itemView.findViewById(R.id.replyButton);
            viewRepliesButton = itemView.findViewById(R.id.viewRepliesButton);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
        }

        public void bind(Comment comment) {
            userNameTextView.setText(comment.getUserName() != null ?
                    comment.getUserName() : comment.getUserEmail());
            commentTextView.setText(comment.getCommentText());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            String dateString = sdf.format(new Date(comment.getTimestamp()));
            timestampTextView.setText(dateString);

            if (!comment.isReply() && comment.getRating() > 0) {
                ratingContainer.setVisibility(View.VISIBLE);
                ratingBar.setRating((float) comment.getRating());
            } else {
                ratingContainer.setVisibility(View.GONE);
            }

            replyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClick(comment);
                }
            });

            if (comment.getReplyCount() > 0) {
                viewRepliesButton.setVisibility(View.VISIBLE);
                viewRepliesButton.setText("View " + comment.getReplyCount() +
                        (comment.getReplyCount() == 1 ? " reply" : " replies"));
                viewRepliesButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewRepliesClick(comment);
                    }
                });
            } else {
                viewRepliesButton.setVisibility(View.GONE);
            }
        }
    }
}