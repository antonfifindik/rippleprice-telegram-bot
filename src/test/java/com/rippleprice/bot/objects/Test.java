package com.rippleprice.bot.objects;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {
	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/#markets").get();

			ArrayList<String> downServers = new ArrayList<>();
			Element table = doc.select("table").get(0); // select the first table.
			Elements rows = table.select("tr");
			StringBuilder result = new StringBuilder();

			for (int i = 1; i < 31; i++) {
				Element row = rows.get(i);
				Elements cols = row.select("td");
				String[] data = cols.text().split(" ");
				data[data.length - 3] = "$" + new DecimalFormat("#0.00").format(Math.rint(100.0 * new Double(data[data.length - 3].substring(1))) / 100.0);
				result.append(data[data.length - 3].replace(',', '.') + " - ");
				result.append(data[1]);
				int k = 1;

				while (!data[++k].startsWith("XRP")) {
					result.append(" " + data[k]);
				}
				result.append("\n");
			}

			result.append("\ninformation by coinmarketcap.com");
			System.out.println(result.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
