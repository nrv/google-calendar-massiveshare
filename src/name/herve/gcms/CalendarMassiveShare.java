/*
 * Copyright 2013 Nicolas HERVE.
 * 
 * This file is part of google-calendar-massiveshare
 * 
 * google-calendar-massiveshare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * google-calendar-massiveshare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with google-calendar-massiveshare. If not, see <http://www.gnu.org/licenses/>.
 */
package name.herve.gcms;

import java.io.IOException;
import java.util.Map;

/**
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class CalendarMassiveShare {
	public static void main(String[] args) {
		try {
			CalendarWrapper calendar = new CalendarWrapper();
			calendar.init();

			for (Map<String, String> c : calendar.getCalendars()) {
				System.out.println("Calendar : " + c.get("id") + " / " + c.get("summary") + " / " + c.get("description"));
			}
			
			String cal = "ENTER YOUR CALENDAR ID HERE";
			
			System.out.println("\n\nWorking on " + cal);
			AccessList list = calendar.getAccessList(cal);
			AccessList.dump(list, "previous.csv");
			
			AccessList list2 = AccessList.load("new.csv");
			
			ACLModifier mod = new ACLModifier();
			// Remove debug mode to -* REALLY *- update ACLs
			// mod.setDebug(false);
			
			calendar.updateAccessList(cal, list, list2, mod);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
