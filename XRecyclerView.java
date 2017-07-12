package com.zgwong.android.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by zgwong on 2017/6/13.
 * attach headers or footers to RecyclerView
 */
public final class XRecyclerView extends RecyclerView {
    private static final int BASE_KEY_HEADER = -10_000;
    private static final int BASE_KEY_FOOTER = -50_000;

    private final SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private final SparseArrayCompat<View> mFooterViews = new SparseArrayCompat<>();

    private int mHeaderType = BASE_KEY_HEADER;
    private int mFooterType = BASE_KEY_FOOTER;

    private MockAdapter mAdapter;

    public XRecyclerView(Context context) {
        super(context);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapter == null) {
            mAdapter = new MockAdapter(mHeaderViews, mFooterViews);
            super.setAdapter(mAdapter);
        }
        mAdapter.bindAdapter(adapter);
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    private void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    // header
    public void addHeaderView(View header) {
        if (header != null) {
            int key = --mHeaderType;
            if (key > BASE_KEY_FOOTER) {
                mHeaderViews.put(key, header);
                notifyDataSetChanged();
            }
        }
    }

    public void removeHeaderView(View header) {
        if (header != null) {
            int idx = mHeaderViews.indexOfValue(header);
            if (idx != -1) {
                mHeaderViews.removeAt(idx);
                notifyDataSetChanged();
            }
        }
    }

    public void removeAllHeaderViews() {
        if (mHeaderViews.size() != 0) {
            mHeaderViews.clear();
            notifyDataSetChanged();
        }
    }

    public int getHeaderViewCount() {
        return mHeaderViews.size();
    }

    // footer
    public void addFooterView(View footer) {
        if (footer != null) {
            mFooterViews.put(--mFooterType, footer);
            notifyDataSetChanged();
        }
    }

    public void removeFooterView(View footer) {
        if (footer != null) {
            int idx = mFooterViews.indexOfValue(footer);
            if (idx != -1) {
                mFooterViews.removeAt(idx);
                notifyDataSetChanged();
            }
        }
    }

    public void removeAllFooterViews() {
        if (mFooterViews.size() != 0) {
            mFooterViews.clear();
            notifyDataSetChanged();
        }
    }

    public int getFooterViewCount() {
        return mFooterViews.size();
    }

    private final class MockAdapter extends Adapter<ViewHolder> {
        private final SparseArrayCompat<View> EMPTY = new SparseArrayCompat<>();

        private SparseArrayCompat<View> innerHeaders;
        private SparseArrayCompat<View> innerFooters;
        private Adapter innerAdapter;

        public MockAdapter(SparseArrayCompat<View> headers, SparseArrayCompat<View> footers) {
            innerHeaders = headers == null ? EMPTY : headers;
            innerFooters = footers == null ? EMPTY : footers;
        }

        // real adapter
        final void bindAdapter(Adapter adapter) {
            innerAdapter = adapter;
        }

        final Adapter getAdapter() {
            return innerAdapter;
        }

        // headers
        final int getHeadersCount() {
            return innerHeaders.size();
        }

        final boolean isHeader(int position) {
            return position < getHeadersCount();
        }

        // footers
        final int getFootersCount() {
            return innerFooters.size();
        }

        final boolean isFooter(int position) {
            return position >= (getHeadersCount() + getRealItemCount());
        }

        // real items
        final int getRealItemCount() {
            return innerAdapter != null ? innerAdapter.getItemCount() : 0;
        }

        final boolean isRealItems(int position) {
            return position >= getHeadersCount() && position < (getHeadersCount() + getRealItemCount());
        }

        final int getRealItemPosition(int position) {
            return position - getHeadersCount();
        }

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View header = innerHeaders.get(viewType);
            if (header != null) {
                return new ViewHolder(header) {};
            }

            View footer = innerFooters.get(viewType);
            if (footer != null) {
                return new ViewHolder(footer) {};
            }

            if (innerAdapter != null) {
                return innerAdapter.onCreateViewHolder(parent, viewType);
            }
            return null;
        }

        @Override
        public final void onBindViewHolder(ViewHolder holder, int position) {
            if (isRealItems(position) && innerAdapter != null) {
                innerAdapter.onBindViewHolder(holder, getRealItemPosition(position));
            }
        }

        @Override
        public final int getItemCount() {
            return getHeadersCount() + getFootersCount() + getRealItemCount();
        }

        @Override
        public final int getItemViewType(int position) {
            if (isHeader(position)) {
                return innerHeaders.keyAt(getHeadersCount() - 1 - position);
            }
            if (isFooter(position)) {
                int size = getRealItemCount();
                return innerFooters.keyAt(getFootersCount() - 1 - (position  - getHeadersCount() - size));
            }
            if (innerAdapter != null) {
                innerAdapter.getItemViewType(getRealItemPosition(position));
            }
            return super.getItemViewType(position);
        }

        @Override
        public final long getItemId(int position) {
            return isRealItems(position) && innerAdapter != null ? innerAdapter.getItemId(position) : super.getItemId(position);
        }

        // override
        @Override
        public final void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (isRealItems(position) && innerAdapter != null) {
                innerAdapter.onBindViewHolder(holder, getRealItemPosition(position), payloads);
            }
        }

        @Override
        public final void setHasStableIds(boolean hasStableIds) {
            if (innerAdapter != null) {
                innerAdapter.setHasStableIds(hasStableIds);
            }
            super.setHasStableIds(hasStableIds);
        }

        @Override
        public final void onViewRecycled(ViewHolder holder) {
            if (innerAdapter != null) {
                innerAdapter.onViewRecycled(holder);
            }
            super.onViewRecycled(holder);
        }

        @Override
        public final boolean onFailedToRecycleView(ViewHolder holder) {
            return super.onFailedToRecycleView(holder) && innerAdapter != null && innerAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public final void onViewAttachedToWindow(ViewHolder holder) {
            if (innerAdapter != null) {
                innerAdapter.onViewAttachedToWindow(holder);
            }
            super.onViewAttachedToWindow(holder);

            int position = holder.getLayoutPosition();
            if (isHeader(position) || isFooter(position)) {
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
                }
            }
        }

        @Override
        public final void onViewDetachedFromWindow(ViewHolder holder) {
            if (innerAdapter != null) {
                innerAdapter.onViewDetachedFromWindow(holder);
            }
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public final void registerAdapterDataObserver(AdapterDataObserver observer) {
            if (innerAdapter != null) {
                innerAdapter.registerAdapterDataObserver(observer);
            }
            super.registerAdapterDataObserver(observer);
        }

        @Override
        public final void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            if (innerAdapter != null) {
                innerAdapter.unregisterAdapterDataObserver(observer);
            }
            super.unregisterAdapterDataObserver(observer);
        }

        @Override
        public final void onAttachedToRecyclerView(RecyclerView recyclerView) {
            if (innerAdapter != null) {
                innerAdapter.onAttachedToRecyclerView(recyclerView);
            }
            super.onAttachedToRecyclerView(recyclerView);
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (isHeader(position) || isFooter(position)) {
                            return gridLayoutManager.getSpanCount();
                        }
                        if (spanSizeLookup != null) {
                            return spanSizeLookup.getSpanSize(position);
                        }
                        return 1;
                    }
                });
                gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
            }
        }

        @Override
        public final void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            if (innerAdapter != null) {
                innerAdapter.onDetachedFromRecyclerView(recyclerView);
            }
            super.onDetachedFromRecyclerView(recyclerView);
        }
    }
}
