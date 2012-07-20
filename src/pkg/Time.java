package pkg;

import java.io.Serializable;

public class Time implements Serializable{
	private static final long serialVersionUID = 1L;
	public int start_hour;
	public int start_minute;
	public int end_hour;
	public int end_minute;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Time){
			Time t = (Time) obj;

			return (t.start_hour == this.start_hour && t.start_minute == this.start_minute &&
					t.end_hour == this.end_hour && t.end_minute == this.end_minute);
		}

		return false;
	}
}
