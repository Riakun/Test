package helperClasses;

import java.io.Serializable;

public class Utile implements Serializable {
	public static int anonymousCounter = 0;

	public static int getAnonCounter() {
		int x = anonymousCounter;
		anonymousCounter++;
		return x;
	}
}
