package sour.project.be.adapters;

import java.util.List;

import sour.project.be.ControlActivity;
import sour.project.be.R;
import sour.project.be.data.Computer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ComputersAdapter extends
		RecyclerView.Adapter<ComputersAdapter.ComputerViewHolder> {

	public static class ComputerViewHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener {

		protected TextView tvComputerName, tvComputerIp, tvComputerOnline;
		protected String ipAddress;

		public ComputerViewHolder(View itemView) {
			super(itemView);
			tvComputerName = (TextView) itemView
					.findViewById(R.id.tvComputerName);
			tvComputerIp = (TextView) itemView.findViewById(R.id.tvComputerIp);
			tvComputerOnline = (TextView) itemView
					.findViewById(R.id.tvComputerOnline);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(v.getContext(), ControlActivity.class);
			intent.putExtra("name", tvComputerName.getText().toString());
			intent.putExtra("ipAddress", ipAddress);
			v.getContext().startActivity(intent);
		}


	}

	Context mContext;
	List<Computer> computers;
	RecyclerView rv;

	public ComputersAdapter(Context context, List<Computer> objects, RecyclerView rclv) {
		computers = objects;
		mContext = context;
		rv = rclv;
	}

	@Override
	public int getItemCount() {
		return computers.size();
	}

	@Override
	public void onBindViewHolder(ComputerViewHolder contactViewHolder, int i) {
		Computer c = computers.get(i);
		contactViewHolder.tvComputerName.setText(c.getName());
		contactViewHolder.tvComputerIp.setText(c.getIpAddress());
		contactViewHolder.ipAddress = c.getIpAddress();
		boolean online = c.isOnline();
		if (online) {
			contactViewHolder.tvComputerOnline.setText("IN USE");
			contactViewHolder.tvComputerOnline.setTextColor(Color
					.parseColor("#00CC00"));
		} else {
			contactViewHolder.tvComputerOnline.setText("OFFLINE");
			contactViewHolder.tvComputerOnline.setTextColor(Color
					.parseColor("#CC0000"));
		}
	}

	@Override
	public ComputerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.card_view_computers, viewGroup, false);
		return new ComputerViewHolder(v);
	}

}
