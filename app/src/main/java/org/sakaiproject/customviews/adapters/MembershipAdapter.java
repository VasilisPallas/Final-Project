package org.sakaiproject.customviews.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;

import org.sakaiproject.api.site.SiteData;
import org.sakaiproject.api.site.actions.IUnJoin;
import org.sakaiproject.api.user.User;
import org.sakaiproject.general.Actions;
import org.sakaiproject.sakai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasilis on 1/18/16.
 */
public class MembershipAdapter extends RecyclerView.Adapter<MembershipAdapter.ViewHolder> implements Filterable {

    private List<SiteData> memberships;
    private List<SiteData> filteredMembership;
    private MembershipFilter filter;
    private Context context;
    private IUnJoin siteUnJoin;

    /**
     * MembershipAdapter constructor
     *
     * @param membership the list with the sites and projects
     * @param context    the context
     */
    public MembershipAdapter(List<SiteData> membership, Context context, IUnJoin siteUnJoin) {
        this.memberships = membership;
        filteredMembership = new ArrayList<>();
        this.context = context;
        this.siteUnJoin = siteUnJoin;
    }

    public void setMemberships(List<SiteData> memberships) {
        this.memberships = memberships;
    }

    @Override
    public MembershipAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.site_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);

        vh.name = (TextView) v.findViewById(R.id.membership_name);
        vh.roster = (TextView) v.findViewById(R.id.membership_roster);
        vh.description = (org.sakaiproject.customviews.TextViewWithImages) v.findViewById(R.id.membership_description);
        vh.unjoin = (ImageView) v.findViewById(R.id.unjoin_imageview);
        vh.unjoinLayout = (FrameLayout) v.findViewById(R.id.unjoin);
        vh.ripple = (MaterialRippleLayout) v.findViewById(R.id.unjoin_ripple);

        return vh;
    }

    @Override
    public void onBindViewHolder(MembershipAdapter.ViewHolder holder, final int position) {

        holder.name.setText(memberships.get(position).getTitle());
        if (holder.roster.getText().toString().length() == 0) {
            holder.roster.setVisibility(View.GONE);
        } else {
            //holder.roster.setText("");
        }

        List<String> sounds = Actions.findAudios(memberships.get(position).getDescription());

        String description = Actions.deleteHtmlTags(memberships.get(position).getDescription());

        if (description.equals("")) {
            holder.description.setText(context.getString(R.string.no_description));
        } else {

            holder.description.setSiteData(memberships.get(position));

            if (description.length() > 60) {
                description = description.substring(0, 60);
                description += "...";
            }
            holder.description.setText(description);
        }

        if (memberships.get(position).getOwner().equals(User.getUserId())) {

            holder.unjoinLayout.setVisibility(View.INVISIBLE);
            holder.ripple.setVisibility(View.INVISIBLE);

        } else {

            holder.unjoinLayout.setVisibility(View.VISIBLE);
            holder.ripple.setVisibility(View.VISIBLE);

            holder.unjoin.setImageDrawable(Actions.setCustomDrawableColor(context, R.mipmap.ic_unjoin, Color.parseColor("#FE6363")));

            holder.unjoinLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(((AppCompatActivity) context).getSupportActionBar().getThemedContext());

                    adb.setTitle(context.getResources().getString(R.string.unjoin_message) + " " + memberships.get(position).getTitle() + " ?");

                    adb.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            siteUnJoin.siteUnJoin(memberships, position);
                        }
                    });

                    adb.setNegativeButton(context.getResources().getString(R.string.cancel), null);

                    Dialog d = adb.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return memberships.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView unjoin;
        TextView name, roster;
        org.sakaiproject.customviews.TextViewWithImages description;
        FrameLayout unjoinLayout;
        com.balysv.materialripple.MaterialRippleLayout ripple;

        public ViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new MembershipFilter();
        }
        return filter;
    }

    private class MembershipFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            if (filteredMembership.size() == 0) {
                synchronized (this) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            filteredMembership.clear();
                            filteredMembership.addAll(memberships);
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            }

            if (constraint.length() == 0 || constraint.equals("")) {
                results.values = filteredMembership;
                results.count = filteredMembership.size();
                return results;
            } else {
                List<SiteData> temp = new ArrayList<>();
                for (SiteData data : filteredMembership) {
                    if (data.getTitle().toLowerCase().contains(constraint)) {
                        temp.add(data);
                    }
                }
                results.values = temp;
                results.count = temp.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (results.values != null) {
                memberships = (ArrayList<SiteData>) results.values;
            } else {
                memberships = new ArrayList<>();
            }

            notifyDataSetChanged();
        }
    }

}
