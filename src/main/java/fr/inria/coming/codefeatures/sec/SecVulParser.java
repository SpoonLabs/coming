package fr.inria.coming.codefeatures.sec;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SecVulParser {

	public Map<String, VulInstance> parse(File pathToVulCSV) throws IOException {

		Map<String, VulInstance> vulfound = new HashMap<>();

		Reader in = new FileReader(pathToVulCSV);
		Iterable<CSVRecord> records = CSVParser.parse(in, CSVFormat.DEFAULT);
		for (CSVRecord record : records) {

			String commitId = record.get(0);
			String cve = record.get(1);
			String project = record.get(2);
			String vtype = record.get(3);

			VulInstance vi = new VulInstance(commitId, project, cve, vtype);

			if (record.size() > 5) {
				String cwe = record.get(4);
				vi.setCWE(cwe);
				String cwetype = record.get(5);
				vi.setCWEType(cwetype);
			}

			vulfound.put(commitId, vi);
		}

		return vulfound;

	}
}
