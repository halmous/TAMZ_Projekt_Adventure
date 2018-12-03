package com.example.jhalm.tamz_projekt_adventure;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class InnerScoreAdapter extends ArrayAdapter<Highscore.DTO>
{
    private List<Highscore.DTO> data;
    private Context context;

    public InnerScoreAdapter(Context context, List<Highscore.DTO> data)
    {
        super(context, R.layout.high_score_map_list_layout, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder holder = null;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.high_score_map_list_layout, parent, false);
            holder = new Holder();
            holder.playerName = convertView.findViewById(R.id.list_player_name);
            holder.score = convertView.findViewById(R.id.list_score);
            holder.time = convertView.findViewById(R.id.list_time);
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }

        Highscore.DTO dto = this.data.get(position);

        holder.playerName.setText("Player: " + dto.player);
        holder.time.setText("Time: " +  Long.toString(dto.time / 60000) + ":" + Long.toString((dto.time % 60000) / 1000) + "." + Long.toString(dto.time % 1000));
        holder.score.setText("Score: " + Integer.toString(dto.score));

        return convertView;
    }

    private static class Holder
    {
        public TextView playerName;
        public TextView score;
        public TextView time;
    }
}
