package couk.rob4001.util.cardboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;

public class CardboardFireworkEffect implements Serializable {

	private static final long serialVersionUID = -3003249695755611357L;

	private List<Integer> colors;
	private boolean trail;
	private boolean flicker;
	private ArrayList<Integer> fade;

	private Type effect;

	public CardboardFireworkEffect(FireworkEffect fe) {
		this.effect = fe.getType();
		this.flicker = fe.hasFlicker();
		this.trail = fe.hasTrail();

		this.colors = new ArrayList<Integer>();
		this.fade = new ArrayList<Integer>();
		for (Color c : fe.getColors()) {
			this.colors.add(c.asRGB());
		}
		for (Color c : fe.getFadeColors()) {
			this.fade.add(c.asRGB());
		}
	}

	public FireworkEffect unbox() {
		Builder b = FireworkEffect.builder();

		b.trail(this.trail).flicker(this.flicker).with(this.effect);

		ArrayList<Color> tc = new ArrayList<Color>();

		for (Integer i : this.colors) {
			tc.add(Color.fromRGB(i));
		}
		b.withColor(tc);

		tc.clear();

		for (Integer i : this.fade) {
			tc.add(Color.fromRGB(i));
		}
		b.withFade(tc);

		return b.build();
	}

}
