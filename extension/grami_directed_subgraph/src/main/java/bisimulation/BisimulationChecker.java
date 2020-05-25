package bisimulation;

import Dmine.Rule;
import bisimulation.bisimulation.BisimilarComputation;
import bisimulation.bisimulation.lts.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class BisimulationChecker {
	private Process processP;
	private Process processQ;
	private BisimilarComputation compute;
	private Rule ruleP;
	private Rule ruleQ;

	public BisimulationChecker(Rule ruleP, Rule ruleQ) {
        this.ruleP = ruleP;
        this.ruleQ = ruleQ;
    }

    public boolean isBisimulation() {
	    this.readInput();
	    this.performBisimulation();
	    return this.compute.isBisimilar();
    }


	/**
	 * This method reads the contents of the files pointed to by the Strings
	 * fileP and fileQ. If any argument is null (or a file with that name does
	 * not exist), the user is (repeatedly) asked to enter a filename until a
	 * valid file is found.
	 * 
	 *
     */
	public void readInput() {
		try {
			this.processP = Process.parse(ruleP, "Process P", "p");
			this.processQ = Process.parse(ruleQ, "Process Q", "q");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * performs partition-based LTS bisimulation. This method must check if
	 * valid input files (us- ing the readInput() method) have been read by the
	 * BisimulationChecker object. If files have not yet been read, display an
	 * error message on the console.
	 */
	public void performBisimulation() {
		this.compute = new BisimilarComputation(this.processP, this.processQ);
	}

}
