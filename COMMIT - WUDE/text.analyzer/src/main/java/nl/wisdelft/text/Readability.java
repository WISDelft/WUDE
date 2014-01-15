/**
 * 
 */
package nl.wisdelft.text;


/**
 * @author oosterman
 */
public class Readability extends de.drni.readability.phantom.Readability {

	private Stats stats;
	
	public Readability(Stats stats) {
		super(stats);
		this.stats = stats;		
	}

	public double calcFlesch() {
		return super.calcFlesch();
	}

	public double calcKincaid() {
		return super.calcKincaid();
	}
	
	public Stats getStats(){
		return stats;
	}

}
