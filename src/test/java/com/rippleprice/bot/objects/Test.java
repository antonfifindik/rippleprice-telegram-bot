package com.rippleprice.bot.objects;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {
	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/").get();
			String[] priceChange = doc.select(".text-large2").text().split(" ");
			System.err.println(priceChange[1]);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
