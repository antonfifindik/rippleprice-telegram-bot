package com.rippleprice.bot.objects;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

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

		Message msg = arg0.getMessage();
		Document doc;

		if (msg != null && msg.hasText()) {
			if (msg.getText().equals("/help"))
				sendMsg(msg, "/price - current price of a Ripple\n/markets - Markets price\n/exmo - Exmo.com price\n/history - Weekly history\n/top10 - Top 10 cryptocurrency market capitalizations");
			if (msg.getText().equals("/price") || msg.getText().equals("/price@RipplePrice_bot")) {
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/").get();
					String[] marketCap = doc.select(".coin-summary-item-detail.details-text-medium>span").text().split(" ");
					String[] priceChange = doc.select(".text-large2").text().split(" ");
					sendMsg(msg, "Current price: " + doc.select("span#quote_price").text() + "\n" + "Price change: " + priceChange[1] + "\n" + "———————————" + "\n" + "Market Cap: " + "\n" + marketCap[0] + " " + marketCap[1] + "\n" + marketCap[2]
							+ " " + marketCap[3] + "\n" + "———————————" + "\n" + "Volume (24h): " + "\n" + marketCap[4] + " " + marketCap[5] + "\n" + marketCap[6] + " " + marketCap[7] + "\n\n" + "information by coinmarketcap.com");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/history") || msg.getText().equals("/history@RipplePrice_bot")) {
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/historical-data/").get();
					Element table = doc.select("table").get(0);
					Elements rows = table.select("tr");
					StringBuilder result = new StringBuilder();

					for (int i = 1; i < 8; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						result.append(data[0] + " " + data[1] + " " + data[2] + "\n" + "High:\n" + data[4] + " USD" + "\nLow:\n" + data[5] + " USD" + "\nMarket Cap:\n" + data[8] + " USD");
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
					StringBuilder result = new StringBuilder();

					for (int i = 1; i < 31; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");

						try {
							data[data.length - 3] = "$" + new DecimalFormat("#0.00").format(Math.rint(100.0 * new Double(data[data.length - 3].substring(1))) / 100.0);
						} catch (Exception e) {
							e.printStackTrace();
						}

						result.append(data[data.length - 3].replace(',', '.') + "  -  ");
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
					doc = Jsoup.connect("https://coinmarketcap.com/").get();
					Element table = doc.select("table").get(0);
					Elements rows = table.select("tr");
					StringBuilder result = new StringBuilder();

					for (int i = 1; i < 11; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						int k = 2;
						result.append(data[0] + ". " + data[2]);

						while (!data[++k].startsWith("$"))
							result.append(" " + data[k]);

						result.append(" (" + data[1] + ")\n");
						result.append("Price:\n" + data[k + 1]);
						result.append("\nMarket Cap:\n" + data[k]);
						result.append("\nChange (24h):\n" + data[data.length - 1]);
						if (i < 10)
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
					StringBuilder result = new StringBuilder();
					ArrayList<String[]> pairs = new ArrayList<>();

					for (int i = 1; i < rows.size(); i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						if (data[1].equals("Ripple") && (data[2].equals("XRP/USD") || data[2].equals("XRP/BTC"))) {
							int resultLength = result.length();

							result.append("Pair: " + data[2] + "\nPrice: " + data[4] + "\nVolume(24h):\n" + data[3] + "\nVolume(%):\n" + data[5]);
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
