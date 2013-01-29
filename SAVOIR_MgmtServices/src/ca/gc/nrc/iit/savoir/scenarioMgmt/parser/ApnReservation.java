// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.scenarioMgmt.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an APN reservation
 * 
 * @author Aaron Moss
 */
public class ApnReservation {

	/**
	 * Available types of APN reservations
	 */
	public static enum ApnReservationMethod {
		CHRONOS_RESERVATION,
		SCENARIO_RESERVATION,
		HARMONY_RESERVATION;
	}
	
	/**
	 * Parameters of a single APN connection
	 */
	public static class ApnConnection {
		private int connectionId;
		private String minBandwidth;
		private String maxBandwidth;
		
		private int sourceId;
		private String sourceName;
		private int destId;
		private String destName;
		
		public ApnConnection() {}

		public ApnConnection(int connectionId, String minBandwidth,
				String maxBandwidth, int sourceId, String sourceName,
				int destId, String destName) {
			
			super();
			this.connectionId = connectionId;
			this.minBandwidth = minBandwidth;
			this.maxBandwidth = maxBandwidth;
			this.sourceId = sourceId;
			this.sourceName = sourceName;
			this.destId = destId;
			this.destName = destName;
		}

		public int getConnectionId() {
			return connectionId;
		}

		public String getMinBandwidth() {
			return minBandwidth;
		}

		public String getMaxBandwidth() {
			return maxBandwidth;
		}

		public int getSourceId() {
			return sourceId;
		}

		public String getSourceName() {
			return sourceName;
		}

		public int getDestId() {
			return destId;
		}

		public String getDestName() {
			return destName;
		}

		public void setConnectionId(int connectionId) {
			this.connectionId = connectionId;
		}

		public void setMinBandwidth(String minBandwidth) {
			this.minBandwidth = minBandwidth;
		}

		public void setMaxBandwidth(String maxBandwidth) {
			this.maxBandwidth = maxBandwidth;
		}

		public void setSourceId(int sourceId) {
			this.sourceId = sourceId;
		}

		public void setSourceName(String sourceName) {
			this.sourceName = sourceName;
		}

		public void setDestId(int destId) {
			this.destId = destId;
		}

		public void setDestName(String destName) {
			this.destName = destName;
		}
	}
	
	private ApnReservationMethod reservationMethod;
	private List<ApnConnection> connections;
	
	public ApnReservation() {}

	public ApnReservationMethod getReservationMethod() {
		return reservationMethod;
	}

	public List<ApnConnection> getConnections() {
		return connections;
	}

	public void setReservationMethod(ApnReservationMethod reservationMethod) {
		this.reservationMethod = reservationMethod;
	}

	public void setConnections(List<ApnConnection> connections) {
		this.connections = connections;
	}
	
	public void addConnnection(ApnConnection connection) {
		if (this.connections == null) {
			this.connections = new ArrayList<ApnConnection>();
		}
		
		this.connections.add(connection);
	}
}
