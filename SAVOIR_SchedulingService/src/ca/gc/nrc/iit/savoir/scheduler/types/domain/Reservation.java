// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scheduler.types.domain;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Reservation {

	private List<ReservationService> servicesList = new Vector<ReservationService>();

	private boolean accepted = false;
	
	private int sessionID;

	public Reservation(Reservation clone) {
		this.servicesList = new Vector<ReservationService>();
		this.sessionID = clone.getSessionID();
		for (ReservationService ss : clone.getServicesList()) {
			this.servicesList.add(new ReservationService(ss));
		}
	}

	public Reservation() {

	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public List<ReservationService> getServicesList() {
		return servicesList;
	}

	public void setServicesList(List<ReservationService> servicesList) {
		this.servicesList = servicesList;
	}

	public void slideTimesToFirst(List<Reservation> sList) {
		int maxI = servicesList.size();
		long earliestSessionStartTime = servicesList.get(0).getStartTime()
				.getTimeInMillis();
		for (int i = 1; i < maxI; i++) {
			if (!servicesList.get(i).isAccepted()
					&& servicesList.get(i).getStartTime().getTimeInMillis() < earliestSessionStartTime) {
				System.out.println("Earliest Subsession: " + i);
				earliestSessionStartTime = servicesList.get(i).getStartTime()
						.getTimeInMillis();
			}
		}

		long earliestConflictEndTime = servicesList
				.get(servicesList.size() - 1).getEndTime().getTimeInMillis();

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);

		long slideVector = earliestConflictEndTime - earliestSessionStartTime;

		for (Reservation s : sList) {
			for (ReservationService sub : s.getServicesList()) {
				if (sub.getEndTime().getTimeInMillis() < earliestConflictEndTime
						&& sub.getEndTime().getTimeInMillis() > earliestSessionStartTime) {
					System.out.println(df.format(sub.getEndTime().getTime()));
					earliestConflictEndTime = sub.getEndTime()
							.getTimeInMillis();
					slideVector = earliestConflictEndTime
							- earliestSessionStartTime;
				}
			}
		}

		System.out.println("Moving session by " + slideVector + " ms");

		for (ReservationService subS : servicesList) {
			subS.setEndTime(subS.getEndTime().getTimeInMillis() + slideVector);
			subS.setStartTime(subS.getStartTime().getTimeInMillis()
					+ slideVector);
		}
	}

	public void slideTimesToLast(List<Reservation> sList) {
		int maxI = servicesList.size();
		long earliestSessionStartTime = servicesList.get(0).getStartTime()
				.getTimeInMillis();
		for (int i = 1; i < maxI; i++) {
			if (!servicesList.get(i).isAccepted()
					&& servicesList.get(i).getStartTime().getTimeInMillis() < earliestSessionStartTime) {
				System.out.println("Earliest Subsession: " + i);
				earliestSessionStartTime = servicesList.get(i).getStartTime()
						.getTimeInMillis();
			}
		}

		long latestConflictEndTime = servicesList.get(0).getEndTime()
				.getTimeInMillis();

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);

		long slideVector = latestConflictEndTime - earliestSessionStartTime;

		for (Reservation s : sList) {
			for (ReservationService sub : s.getServicesList()) {
				if (sub.getEndTime().getTimeInMillis() > latestConflictEndTime
						&& sub.getEndTime().getTimeInMillis() > earliestSessionStartTime) {
					System.out.println(df.format(sub.getEndTime().getTime()));
					latestConflictEndTime = sub.getEndTime().getTimeInMillis();
					slideVector = latestConflictEndTime
							- earliestSessionStartTime;
				}
			}
		}

		System.out.println("Moving session by " + slideVector + " ms");

		for (ReservationService subS : servicesList) {
			subS.setEndTime(subS.getEndTime().getTimeInMillis() + slideVector);
			subS.setStartTime(subS.getStartTime().getTimeInMillis()
					+ slideVector);
		}
	}

	public void slide(List<Reservation> sList) {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.SHORT);

		int maxI = servicesList.size();
		long latestBlockedStartTime = Long.MIN_VALUE;
		long earliestConflictingEndTime = Long.MAX_VALUE;
		long slideVector = 0;

		for (int i = 0; i < maxI; i++) {
			if (!servicesList.get(i).isAccepted()
					&& servicesList.get(i).getStartTime().getTimeInMillis() > latestBlockedStartTime) {

				for (Reservation s : sList) {
					for (ReservationService sub : s.getServicesList()) {
						if (sub.getEndTime().getTimeInMillis() < earliestConflictingEndTime
								&& sub.getEndTime().getTimeInMillis() > servicesList
										.get(i).getStartTime()
										.getTimeInMillis()) {
							// System.out.println(df.format(sub.getEndTime().
							// getTime()));
							latestBlockedStartTime = servicesList.get(i)
									.getStartTime().getTimeInMillis();
							earliestConflictingEndTime = sub.getEndTime()
									.getTimeInMillis();
							slideVector = earliestConflictingEndTime
									- latestBlockedStartTime;
						}
					}
				}
			}
		}

		if (slideVector == 0) {
			System.out.println("Session can't be accepted!");
			// TODO RAISE EXCEPTION
			return;
		}

		System.out.println(df.format(new Date(latestBlockedStartTime)));

		// for (Session s : sList) {
		// for (SubSession sub : s.getSubSessionsList()) {
		// if (sub.getEndTime().getTimeInMillis() < earliestConflictingEndTime
		// && sub.getEndTime().getTimeInMillis() > latestBlockedStartTime) {
		// // System.out.println(df.format(sub.getEndTime().getTime()));
		// earliestConflictingEndTime = sub.getEndTime()
		// .getTimeInMillis();
		// slideVector = earliestConflictingEndTime
		// - latestBlockedStartTime;
		// }
		// }
		// }
		// if (earliestConflictingEndTime == Long.MAX_VALUE) {
		// System.out.println("No conflict!");
		// return;
		// }

		System.out.println(df.format(new Date(earliestConflictingEndTime)));

		System.out.println("Moving session by " + slideVector + " ms");

		for (ReservationService subS : servicesList) {
			subS.setEndTime(subS.getEndTime().getTimeInMillis() + slideVector);
			subS.setStartTime(subS.getStartTime().getTimeInMillis()
					+ slideVector);
		}
	}

	public void applySlidingVector(long slideVector) {
		for (ReservationService subS : servicesList) {
			subS.setEndTime(subS.getEndTime().getTimeInMillis() + slideVector);
			subS.setStartTime(subS.getStartTime().getTimeInMillis()
					+ slideVector);
		}
	}

	public void slideTimesToLast() {
		int maxI = servicesList.size();
		Calendar origin = servicesList.get(0).getEndTime();

		int ind = 0;
		for (int i = 0; i < maxI; i++) {
			if (!servicesList.get(i).isAccepted()
					&& servicesList.get(i).getEndTime().compareTo(origin) > 0) {
				origin.setTime(servicesList.get(i).getEndTime().getTime());
				ind = i;
			}
		}

		long slide = servicesList.get(ind).getEndTime().getTimeInMillis()
				- servicesList.get(ind).getStartTime().getTimeInMillis();

		System.out.println("Moving session by " + slide + " ms");

		for (ReservationService subS : servicesList) {
			subS.setEndTime(subS.getEndTime().getTimeInMillis() + slide);
			subS.setStartTime(subS.getStartTime().getTimeInMillis() + slide);
		}
	}

	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}

	public int getSessionID() {
		return sessionID;
	}

	// public String toString() {
	// StringBuffer str = new StringBuffer();
	// DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
	// DateFormat.SHORT);
	//
	// //
	// "S\t|SubS\t|C\t|EP1\t\t\t|EP2\t\t\t|BW\t|Lambda\t|Path\t\t\t|Start Time\t|End Time\n"
	// // ;
	// int maxI = this.getSubSessionsList().size();
	// for (int j = 0; j < maxI; j++) {
	// int maxK = this.getSubSessionsList().get(j).getConnectionsList().size();
	// for (int k = 0; k < maxK; k++) {
	// str.append("_\t|");
	// str.append(j + "\t|");
	// Connection c =
	// this.getSubSessionsList().get(j).getConnectionsList().get(k);
	// str.append(k + "\t|" + c.getSourceTriple().toString() + "\t|" +
	// c.getDestinationTriple().toString() + "\t|");
	// str.append(c.getBandwidth() + "\t|");
	// if (this.getSubSessionsList().get(j).getConnectionsList().get(k).
	// getAssignedLambda() != -1) {
	// str.append(this.getSubSessionsList().get(j).getConnectionsList().get(k).
	// getAssignedLambda() + "\t|");
	// } else {
	// str.append("N/A" + "\t|");
	//
	// }
	//
	// if (this.getSubSessionsList().get(j).getConnectionsList().get(k).
	// getAssignedPath().get_vertices().size() != 0) {
	// str.append(this.getSubSessionsList().get(j).getConnectionsList().get(k).
	// getAssignedPath() + "\t\t\t|");
	// } else {
	// str.append("N/A" + "\t\t\t|");
	// }
	//
	// str.append(df.format(this.getSubSessionsList().get(j).getStartTime().
	// getTime()) + "\t|"
	// + df.format(this.getSubSessionsList().get(j).getEndTime().getTime()));
	// str.append("\n");
	// }
	// }
	// return str.toString();
	// }
	//
	// public String getStats() {
	// String ret = "" + getSubSessionsList().size();
	// int nbCon = 0;
	// int nbBS = 0;
	// int nbBSub = 0;
	// int nbBCon = 0;
	// int nbWave = 0;
	// int nbHops = 0;
	//
	// if (!isAccepted())
	// nbBS = 1;
	// for (SubSession ss : getSubSessionsList()) {
	// nbCon += ss.getConnectionsList().size();
	// if (!ss.isAccepted()) {
	// nbBSub++;
	// nbBCon += ss.getConnectionsList().size();
	// } else {
	// for (Connection c : ss.getConnectionsList()) {
	// if (c.getAssignedLambda() > nbWave)
	// nbWave = c.getAssignedLambda();
	// if (c.getAssignedPath().get_vertices().size() != 0) {
	// nbHops += c.getAssignedPath().get_vertices().size() - 1;
	// } else {
	// nbHops += c.getLinks().split("#").length - 1;
	// }
	// }
	// }
	// }
	// // YorN NbSS NbC NbBS NbBSS NbBC NbWaves NbHops
	// return nbBS + "\t" + ret + "\t" + nbCon + "\t" + nbBSub + "\t" + nbBCon +
	// "\t" + nbWave + "\t" + nbHops;
	// }
}
