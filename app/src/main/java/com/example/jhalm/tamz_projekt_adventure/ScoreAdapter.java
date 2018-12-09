package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ScoreAdapter extends ArrayAdapter<Highscore.DTO>
{
    List<Highscore.DTO> data;
    Context context;

    public ScoreAdapter(Context context)
    {
        super(context, R.layout.high_score_layout);
        Highscore highscore = new Highscore(context);
        this.data = highscore.SelectAll();
        this.addAll(this.data);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Holder holder;

        if(convertView == null)
        {
            holder = new Holder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.high_score_layout, parent, false);
            holder.mapName = convertView.findViewById(R.id.list_item_map_name);
            holder.playerName = convertView.findViewById(R.id.list_item_player_name);
            holder.score = convertView.findViewById(R.id.list_item_score);
            holder.time = convertView.findViewById(R.id.list_item_time);
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }

        holder.mapName.setText("Map:" + this.data.get(position).mapName);
        holder.playerName.setText("Player: " + this.data.get(position).player);
        holder.score.setText("Score: " + Integer.toString(this.data.get(position).score));
        holder.time.setText("Time: " + Long.toString(this.data.get(position).time / 60000) + ":" + Long.toString((this.data.get(position).time % 60000) / 1000) + "." + Long.toString(this.data.get(position).time % 1000));

        return convertView;
    }

    private static class Holder
    {
        TextView mapName;
        TextView playerName;
        TextView score;
        TextView time;
    }
}
