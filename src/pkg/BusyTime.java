package pkg;

import java.io.Serializable;

public class BusyTime implements Serializable{
	private static final long serialVersionUID = 1L;
	public Day day = null;
	public Time time = new Time();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BusyTime) {
			BusyTime b = (BusyTime) obj;

			return (b.day == this.day && b.time.equals(this.time));
		}

		return false;
	}
}
