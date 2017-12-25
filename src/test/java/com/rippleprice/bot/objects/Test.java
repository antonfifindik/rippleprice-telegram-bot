package com.rippleprice.bot.objects;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {
	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/historical-data/").get();

			ArrayList<String> downServers = new ArrayList<>();
			Element table = doc.select("table").get(0); // select the first table.
			Elements rows = table.select("tr");
			StringBuilder result = new StringBuilder();

			for (int i = 1; i < 8; i++) {
				Element row = rows.get(i);
				Elements cols = row.select("td");
				String[] data = cols.text().split(" ");
				result.append(data[0] + " " + data[1] + " " + data[2] + "\n" + "High:\n" + data[4] + " USD" + "\nLow:\n" + data[5] + " USD" + "\nMarket Cap:\n" + data[8] + " USD\n———————————\n");
			}

			System.err.println(result.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
