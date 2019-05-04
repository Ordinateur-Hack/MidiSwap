package com.jimdo.dominicdj.midiswap.ruleslist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.jimdo.dominicdj.midiswap.R;

import java.util.ArrayList;
import java.util.List;

// see https://willowtreeapps.com/ideas/android-fundamentals-working-with-the-recyclerview-adapter-and-viewholder-pattern
// and https://hackernoon.com/android-recyclerview-onitemclicklistener-getadapterposition-a-better-way-3c789baab4db
// for great tips on RecyclerViews
public class RulesAdapter extends RecyclerView.Adapter<RulesViewHolder> {

    private List<RulesViewModel> models = new ArrayList<>();
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnClickListener onDeleteClickListener;

    public RulesAdapter(final List<RulesViewModel> viewModels,
                        AdapterView.OnItemSelectedListener onItemSelectedListener,
                        AdapterView.OnClickListener onDeleteClickListener) {
        if (viewModels != null) {
            this.models.addAll(viewModels); // use addAll, so that the list can't be modified outside of this adapter
        }
        this.onItemSelectedListener = onItemSelectedListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public RulesViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(viewType, viewGroup, false);
        return new RulesViewHolder(view, onItemSelectedListener, onDeleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final RulesViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position.
        viewHolder.bindData(models.get(position));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.rules_list_item;
    }

    public void addItem(RulesViewModel newItem) {
        this.models.add(newItem);
        notifyItemInserted(models.size() - 1);
    }

    public void removeItem(RulesViewModel removeItem) {
        if (models.contains(removeItem)) {
            int removeIndex = models.indexOf(removeItem);
            models.remove(removeIndex);
            notifyItemRemoved(removeIndex);
        }
    }

    public void removeItem(int removeIndex) {
        models.remove(removeIndex);
        notifyItemRemoved(removeIndex);
    }

}
