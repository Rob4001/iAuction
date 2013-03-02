package couk.rob4001.util.cardboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;

public class CardboardFireworkEffect implements Serializable{

	private static final long serialVersionUID = -3003249695755611357L;
	
	
	private List<Integer> colors;
	private boolean trail;
	private boolean flicker;
	private ArrayList<Integer> fade;


	private Type effect;
	
	public CardboardFireworkEffect(FireworkEffect fe){
		effect = fe.getType();
		flicker = fe.hasFlicker();
		trail = fe.hasTrail();
		
		colors = new ArrayList<Integer>();
		fade = new ArrayList<Integer>();
		for(Color c:fe.getColors()){
			colors.add(c.asRGB());
		}
		for(Color c:fe.getFadeColors()){
			fade.add(c.asRGB());
		}
	}
	
	public FireworkEffect unbox(){
		Builder b = FireworkEffect.builder();
		
		b.trail(trail).flicker(flicker).with(effect);
		
		ArrayList<Color> tc = new ArrayList<Color>();
		
		for (Integer i:colors){
			tc.add(Color.fromRGB(i));
		}
		b.withColor(tc);
		
		tc.clear();
		
		for (Integer i:fade){
			tc.add(Color.fromRGB(i));
		}
		b.withFade(tc);
		
		
		return b.build();
	}

}
