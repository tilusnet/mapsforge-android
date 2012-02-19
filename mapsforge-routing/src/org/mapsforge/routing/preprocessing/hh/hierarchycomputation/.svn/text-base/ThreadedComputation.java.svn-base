/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.routing.preprocessing.hh.hierarchycomputation;

import java.text.DecimalFormat;

/**
 * @author Frank Viernau
 */
class ThreadedComputation {

	private static final int MSG_INTERVAL = 1000;

	private static final DecimalFormat df = new DecimalFormat("###,###,###");

	public static void executeThreads(ComputationThread[] threads, int totalProgress,
			String description) {
		long start = System.currentTimeMillis();
		double runningTime = 0;
		double progress = 0;
		boolean alive = true;

		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}

		while (alive) {
			try {
				Thread.sleep(MSG_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			alive = false;
			progress = 0;
			for (int i = 0; i < threads.length; i++) {
				alive |= threads[i].isAlive();
				progress += threads[i].progress();
			}
			runningTime = (System.currentTimeMillis() - start);
			System.out.print("\n" + description + " :  "
					+ df.format((progress / totalProgress) * 100) + "%  " + df.format(progress)
					+ "/" + df.format(totalProgress) + "  "
					+ df.format((progress / runningTime) * 1000) + "/s  " + "in "
					+ millisToString(runningTime) + "  " + "ETA "
					+ millisToString((runningTime / progress * (totalProgress - progress))));
		}
		System.out.print("\n");
	}

	private static String millisToString(double millis) {
		if (millis > 1000 * 60 * 60) {
			return df.format(millis / (1000 * 60 * 60)) + "h";
		} else if (millis > 1000 * 60) {
			return df.format(millis / (1000 * 60)) + "m";
		} else if (millis > 1000) {
			return df.format(millis / 1000) + "s";
		} else {
			return df.format(millis) + "ms";
		}
	}
}
