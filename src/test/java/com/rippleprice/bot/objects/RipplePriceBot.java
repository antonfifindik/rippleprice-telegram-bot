package com.rippleprice.bot.objects;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.rippleprice.bot.json.JsonReader;

public class RipplePriceBot extends TelegramLongPollingBot {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		TelegramBotsApi botapi = new TelegramBotsApi();
		try {
			botapi.registerBot(new RipplePriceBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void onUpdateReceived(Update arg0) {

		Locale.setDefault(Locale.ENGLISH);
		Message msg = arg0.getMessage();
		StringBuilder result = new StringBuilder();
		Document doc;
		JSONArray jsonArray;

		if (msg != null && msg.hasText()) {
			if (msg.getText().equals("/help"))
				sendMsg(msg, "/price - current price of a Ripple\n/markets - Markets price\n/exmo - Exmo.com price\n/history - Weekly history\n/top10 - Top 10 cryptocurrency market capitalizations");
			if (msg.getText().equals("/price") || msg.getText().equals("/price@RipplePrice_bot")) {
				try {
					jsonArray = JsonReader.readJsonFromUrl("https://api.coinmarketcap.com/v1/ticker/ripple/");
					JSONObject jsonObject = jsonArray.getJSONObject(0);

					result.append(String.format("Current price: *%s*\nChange(24h): %s%%\n———————————\nMarket cap: \n%s\nRank: %s\n———————————\nVolume(24h):\n%s\nСhange(7d): %s%%", new DecimalFormat("$#0.00").format(jsonObject.getDouble("price_usd")),
							jsonObject.getString("percent_change_24h"), new DecimalFormat("$###,###.###").format(jsonObject.getDouble("market_cap_usd")), jsonObject.getString("rank"),
							new DecimalFormat("$###,###.###").format(jsonObject.getDouble("24h_volume_usd")), jsonObject.getString("percent_change_7d")));

					result.append("\n\ninformation by coinmarketcap.com");
					sendMsg(msg, result.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/history") || msg.getText().equals("/history@RipplePrice_bot")) {
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/historical-data/").get();
					Element table = doc.select("table").get(0);
					Elements rows = table.select("tr");

					for (int i = 1; i < 8; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						result.append(data[0] + " " + data[1] + " " + data[2] + "\n" + "High:\n*$" + data[4] + "*" + "\nLow:\n*$" + data[5] + "*" + "\nMarket Cap:\n" + data[8] + " USD");
						if (i < 7)
							result.append("\n———————————\n");

					}
					result.append("\n\ninformation by coinmarketcap.com");
					sendMsg(msg, result.toString());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/markets") || msg.getText().equals("/markets@RipplePrice_bot")) {
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/#markets").get();
					Element table = doc.select("table").get(0);
					Elements rows = table.select("tr");

					for (int i = 1; i < 31; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");

						try {
							data[data.length - 3] = "$" + new DecimalFormat("#0.00").format(Math.rint(100.0 * new Double(data[data.length - 3].substring(1))) / 100.0);
						} catch (Exception e) {
							e.printStackTrace();
						}

						result.append("*" + data[data.length - 3].replace(',', '.') + "*" + "  -  ");
						result.append(data[1]);
						int k = 1;

						while (!data[++k].startsWith("XRP")) {
							result.append(" " + data[k]);
						}
						result.append("  (" + data[data.length - 5] + ")");
						result.append("\n");
					}

					result.append("\ninformation by coinmarketcap.com");
					sendMsg(msg, result.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/top10") || msg.getText().equals("/top10@RipplePrice_bot")) {
				try {
					jsonArray = JsonReader.readJsonFromUrl("https://api.coinmarketcap.com/v1/ticker/?limit=10");
					for (int i = 0; i < jsonArray.length(); i++) {

						result.append(String.format("%s. *%s* (%s)\nPrice: *$%s*\nMarket Cap:\n%s\nChange(24h):\n%s%%", jsonArray.getJSONObject(i).get("rank"), jsonArray.getJSONObject(i).get("name"), jsonArray.getJSONObject(i).get("symbol"),
								new DecimalFormat("#0.00").format(jsonArray.getJSONObject(i).getDouble("price_usd")).replace(',', '.'), new DecimalFormat("$###,###.###").format(jsonArray.getJSONObject(i).getDouble("market_cap_usd")),
								jsonArray.getJSONObject(i).get("percent_change_24h")));
						if (i < jsonArray.length() - 1)
							result.append("\n———————————\n");
					}

					result.append("\n\ninformation by coinmarketcap.com");
					sendMsg(msg, result.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/exmo") || msg.getText().equals("/exmo@RipplePrice_bot")) {
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/exchanges/exmo/").get();
					Element table = doc.select("table").get(0);
					Elements rows = table.select("tr");
					ArrayList<String[]> pairs = new ArrayList<>();

					for (int i = 1; i < rows.size(); i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						if (data[1].equals("Ripple") && (data[2].equals("XRP/USD") || data[2].equals("XRP/BTC"))) {
							int resultLength = result.length();

							result.append("Pair: " + data[2] + "\nPrice: *" + data[4] + "*\nVolume(24h):\n" + data[3] + "\n" + data[5]);
							if (resultLength == 0)
								result.append("\n———————————\n");
							else
								result.append("\n\ninformation by coinmarketcap.com");
						}
					}

					sendMsg(msg, result.toString());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getBotUsername() {
		return BotConfig.BOT_USERNAME;
	}

	@Override
	public String getBotToken() {
		return BotConfig.BOT_TOKEN;
	}

	private void sendMsg(Message msg, String text) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(msg.getChatId().toString());
		sendMessage.setReplyToMessageId(msg.getMessageId());
		sendMessage.setText(text);
		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
