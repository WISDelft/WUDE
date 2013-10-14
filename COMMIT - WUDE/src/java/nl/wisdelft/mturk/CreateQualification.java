/**
 * 
 */
package nl.wisdelft.mturk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.wisdelft.WUDEUtil;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * @author oosterman
 */
public class CreateQualification {

	private static final String MTURK_PROPERTIES = "mturk.properties";
	private static final String flowerQualificationId = "2EWW5XH0JPPUGAU7X2CSX75KLU8FLR";
	private static final String lieYenSurveyQualificationId = "21VYU98JHSTN9BV9YOA9QOBJER1DPK";
	private static final String ownWorkerId = "AQX94TPUN2WW6";
	static RequesterService service = new RequesterService(new PropertiesClientConfig(WUDEUtil.getResourceFilePath(MTURK_PROPERTIES)));

	public static void assignQualificationToWorkers(List<String> workerIds, String qualificationId, boolean notifyWorker) {
		for (String workerId : workerIds) {
			service.assignQualification(qualificationId, workerId, 1, notifyWorker);
		}
	}

	public static void emailWorkers(List<String> workerIds, String subject, String message){
		service.notifyWorkers(subject, message, workerIds.toArray(new String[]{}));
	}

	public static void main(String[] args) throws Exception {

		/*
		// Get all the workers with the flower qualification Id
		List<String> workerIDs = new ArrayList<String>();
		Qualification[] qualifications = service.getAllQualificationsForQualificationType(flowerQualificationId);
		for (Qualification q : qualifications) {
			// get the worker
			String workerID = q.getSubjectId();
			workerIDs.add(workerID);
		}
		
		// send the message to the workers
		String subject = "Your flower expertise is requested";
		String message = "Hi workers!\n\n"
				+ "In the past you have provided high quality work on my flower HITS. From previous HITs I got a lot of good answers but there were 31 images with disputing anwers. "
				+ "I have created a new batch of these 31 images. I only allow workers with"
				+ " the 'Flower knowledge' qualification. Since you have provided high quality work I have assigned this qualification to you.  \n\n"
				+ "The pay is $0.08/HIT and I will award a bonus if you complete multiple HITs. For more than 15 HITS accepted in this batch "
				+ "I will award a bonus of $0.30. If you get more than 25 accepted in the batch I will award you another $0.30.\n\n"
				+ "Please find the HITs here: https://www.mturk.com/mturk/searchbar?selectedSearchType=hitgroups&requesterId=AQX94TPUN2WW6\n\n"
				+ "Kind regards,\nGeorge Wiser";
		*/
		String subject = "Followup HIT Personality Survey (reminder)";
		String message = "Dear Workers,\n\n"+
				"Thank you again for participating in our experiment. We are building a worker pool for future experiments for which the pay wil be substantial higher. You have performed one of our surveys but only workers who also performed the second survey will be added to our worker pool.\n\n" +
				"We kindly request you to perform the following HIT: https://www.mturk.com/mturk/searchbar?selectedSearchType=hitgroups&searchWords=george+wiser ." +
				"The HIT again pays 5 cents and consists of 4 questions about your personality.\n\n" +
				"Kind regards\nGeorge Wiser"; 

		
		String[] workers = new String[] {"AQX94TPUN2WW6","A2COXKLSVE4M5T","A1XKLW0MI4ZGRK","A2SRSKSRN6NA3E","AI466ICHWPVDR","AP51OABHKXJCO","ASS1U4KZZ7UU8","A32M8A5VFCVIVZ","A3EFOSAZBKKFT","A24H1L0C3EOH6K","A3PW753TPR4PVP","A2IZ2W58WQXPKH","AW9ZX39XCJ4EL","AIUKV0KVALMY6","A1Q4YI26MQV3AY","AIOKE7L4LLE2N","A35UHZIVSLVSEN","A2TI08A5G1AFK3","A1WUQ217QN5Y99","AP642WL6QU5ZQ","A2VDDES6LFZOFW","A3DMJV44OBBFU2","A2CPBDUGX4QF2C","AVHARSCRX7M9E","A16QPKMTZUG0H1","A2FOB0Y5ANZXEA","A2GAFXIEZ1LNXZ","A2ADR4295J3BG4","AGVAY3FTPAVLQ","A2SXLFJQW1DWYI","A73C0SL37RPTQ","A37KKY9Q1JU07N","A2KY5VTNYKHOI6","A3535BGYFACXQP","A9KPCMO1J1LQF","A2WFNPUI1JSPG4","AMUTASURWBRE7","A3VCVGVYKTIC32","AIMGGTVJX45W4","A367O03C6MRMI8","A2IZT3TS307P4B","A16LN4SIR7KTRI","A2CTMC7IWOBUVY","A1Z3BW42X5CRLR","A3S6CBTU1HB21X","AOJF7CPZVK5U3","A21CE7W3K5MUVM","AW378SO6MTG1V","AFG3L1YA5FQNF","A4FMTP47DKZS0","A1L1SQ488YCCFJ","A2K1K7ALOS8YIN","A3KB7V2R1A15FQ","A179OX1482SRXK","A2YPVFWTIB0T0T" };
		//assignQualificationToWorkers(Arrays.asList(workers), lieYenSurveyQualificationId, false);
		emailWorkers(Arrays.asList(workers), subject, message);
	}
}
