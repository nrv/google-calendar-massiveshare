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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class AccessList implements Iterable<String> {
	private final static String SEP = ", ";

	public enum Role {
		owner, writer, reader
	};

	public enum ScopeType {
		user, group, domain
	};

	public AccessList() {
		super();

		rights = new HashMap<String, Role>();
		types = new HashMap<String, ScopeType>();
		ids = new HashMap<String, String>();
	}

	private Map<String, Role> rights;
	private Map<String, ScopeType> types;
	private Map<String, String> ids;

	public static AccessList load(String file) throws IOException {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(file));
			
			AccessList list = new AccessList();
			
			String line;
			while((line = r.readLine()) != null) {
				String[] data = line.split(",");
				if (data.length < 2) {
					throw new IOException("Invalid line in " + file + " : " + line);
				}
				
				if (data.length > 2) {
					list.set(data[0].trim(), AccessList.Role.valueOf(data[1].trim()), AccessList.ScopeType.valueOf(data[2].trim()), null);
				} else {
					list.set(data[0].trim(), AccessList.Role.valueOf(data[1].trim()), AccessList.ScopeType.user, null);
				}
			}
			
			return list;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public static void dump(AccessList list, String file) throws IOException {
		FileWriter w = null;
		try {
			w = new FileWriter(file);

			for (String email : list) {
				ScopeType t = list.getType(email);
				if (t == null) {
					t = ScopeType.user;
				}
				w.write(email + SEP + list.getRight(email) + SEP + t + "\n");
			}
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public void set(String email, Role right, ScopeType type, String id) {
		rights.put(email, right);
		types.put(email, type);
		if (id != null) {
			ids.put(email, id);
		}
	}

	@Override
	public Iterator<String> iterator() {
		return rights.keySet().iterator();
	}

	public Role getRight(String key) {
		return rights.get(key);
	}

	public ScopeType getType(String key) {
		return types.get(key);
	}

	public boolean containsEmail(String key) {
		return rights.containsKey(key);
	}

	public String getId(String key) {
		return ids.get(key);
	}

}
