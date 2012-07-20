package pkg;

import java.io.Serializable;
import java.util.Vector;

public class Schedule implements Serializable{
	private static final long serialVersionUID = 1L;
	public Vector<BusyTime> classes = new Vector<BusyTime>(0);
	public String name = "";

	@Override
	public String toString(){
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Schedule) {
			Schedule s = (Schedule) obj;
			return (s.name.equals(this.name) && s.classes.equals(this.classes));
		}

		return false;
	}
}
