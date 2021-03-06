package com.mgaetan89.showsrage.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mgaetan89.showsrage.R;
import com.mgaetan89.showsrage.adapter.HistoriesAdapter;
import com.mgaetan89.showsrage.helper.GenericCallback;
import com.mgaetan89.showsrage.model.GenericResponse;
import com.mgaetan89.showsrage.model.Histories;
import com.mgaetan89.showsrage.model.History;
import com.mgaetan89.showsrage.network.SickRageApi;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HistoryFragment extends Fragment implements Callback<Histories>, DialogInterface.OnClickListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
	@Nullable
	private HistoriesAdapter adapter = null;

	@Nullable
	private FloatingActionButton clearHistory = null;

	@Nullable
	private TextView emptyView = null;

	@NonNull
	private final List<History> histories = new ArrayList<>();

	@Nullable
	private RecyclerView recyclerView = null;

	@Nullable
	private SwipeRefreshLayout swipeRefreshLayout = null;

	public HistoryFragment() {
	}

	@Override
	public void failure(RetrofitError error) {
		if (this.swipeRefreshLayout != null) {
			this.swipeRefreshLayout.setRefreshing(false);
		}

		error.printStackTrace();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.onRefresh();
	}

	@Override
	public void onClick(View view) {
		if (view == null) {
			return;
		}

		int id = view.getId();

		if (id == R.id.clear_history) {
			this.clearHistory();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		SickRageApi.getInstance().getServices().clearHistory(new ClearHistoryCallback(this.getActivity()));
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		if (view != null) {
			this.clearHistory = (FloatingActionButton) view.findViewById(R.id.clear_history);
			this.emptyView = (TextView) view.findViewById(android.R.id.empty);
			this.recyclerView = (RecyclerView) view.findViewById(android.R.id.list);
			this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

			if (this.recyclerView != null) {
				int columnCount = this.getResources().getInteger(R.integer.shows_column_count);
				this.adapter = new HistoriesAdapter(this.histories);

				this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
						super.onScrolled(recyclerView, dx, dy);

						if (HistoryFragment.this.swipeRefreshLayout != null) {
							HistoryFragment.this.swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
						}
					}
				});
				this.recyclerView.setAdapter(this.adapter);
				this.recyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), columnCount));
			}

			if (this.clearHistory != null) {
				this.clearHistory.setOnClickListener(this);
			}

			if (this.swipeRefreshLayout != null) {
				this.swipeRefreshLayout.setColorSchemeResources(R.color.accent);
				this.swipeRefreshLayout.setOnRefreshListener(this);
			}
		}

		return view;
	}

	@Override
	public void onDestroy() {
		this.histories.clear();

		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		this.clearHistory = null;
		this.emptyView = null;
		this.recyclerView = null;
		this.swipeRefreshLayout = null;

		super.onDestroyView();
	}

	@Override
	public void onRefresh() {
		if (this.swipeRefreshLayout != null) {
			this.swipeRefreshLayout.setRefreshing(true);
		}

		SickRageApi.getInstance().getServices().getHistory(this);
	}

	@Override
	public void success(Histories histories, Response response) {
		if (this.swipeRefreshLayout != null) {
			this.swipeRefreshLayout.setRefreshing(false);
		}

		this.histories.clear();

		if (histories != null) {
			this.histories.addAll(histories.getData());
		}

		if (this.histories.isEmpty()) {
			if (this.clearHistory != null) {
				this.clearHistory.setVisibility(View.GONE);
			}

			if (this.emptyView != null) {
				this.emptyView.setVisibility(View.VISIBLE);
			}

			if (this.recyclerView != null) {
				this.recyclerView.setVisibility(View.GONE);
			}
		} else {
			if (this.clearHistory != null) {
				this.clearHistory.setVisibility(View.VISIBLE);
			}

			if (this.emptyView != null) {
				this.emptyView.setVisibility(View.GONE);
			}

			if (this.recyclerView != null) {
				this.recyclerView.setVisibility(View.VISIBLE);
			}
		}

		if (this.adapter != null) {
			this.adapter.notifyDataSetChanged();
		}
	}

	private void clearHistory() {
		new AlertDialog.Builder(this.getActivity())
				.setMessage(R.string.clear_history_confirm)
				.setPositiveButton(R.string.clear, this)
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}

	private final class ClearHistoryCallback extends GenericCallback {
		private ClearHistoryCallback(FragmentActivity activity) {
			super(activity);
		}

		@Override
		public void success(GenericResponse genericResponse, Response response) {
			super.success(genericResponse, response);

			HistoryFragment fragment = HistoryFragment.this;

			fragment.histories.clear();

			if (fragment.adapter != null) {
				fragment.adapter.notifyDataSetChanged();
			}

			if (fragment.clearHistory != null) {
				fragment.clearHistory.setVisibility(View.GONE);
			}

			if (fragment.emptyView != null) {
				fragment.emptyView.setVisibility(View.VISIBLE);
			}
		}
	}
}
