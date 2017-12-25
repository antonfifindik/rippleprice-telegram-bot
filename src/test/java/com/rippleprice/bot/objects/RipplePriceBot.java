package com.rippleprice.bot.objects;

import java.io.IOException;
import java.text.DecimalFormat;

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
		if (msg != null && msg.hasText()) {
			if (msg.getText().equals("/help"))
				sendMsg(msg, "/price - current price of a Ripple\n/history - Weekly history\\n/markets - Markets price");
			if (msg.getText().equals("/price")) {
				try {
					Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/").get();
					String[] marketCap = doc.select(".coin-summary-item-detail.details-text-medium>span").text().split(" ");
					String[] priceChange = doc.select(".text-large2").text().split(" ");
					sendMsg(msg, "Current price: " + doc.select("span#quote_price").text() + "\n" + "Price change: " + priceChange[1] + "\n" + "———————————" + "\n" + "Market Cap: " + "\n" + marketCap[0] + " " + marketCap[1] + "\n" + marketCap[2]
							+ " " + marketCap[3] + "\n" + "———————————" + "\n" + "Volume (24h): " + "\n" + marketCap[4] + " " + marketCap[5] + "\n" + marketCap[6] + " " + marketCap[7] + "\n\n" + "information by coinmarketcap.com");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/history")) {
				try {
					Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/historical-data/").get();
					Element table = doc.select("table").get(0); // select the first table.
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (msg.getText().equals("/markets")) {
				Document doc;
				try {
					doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/#markets").get();
					Element table = doc.select("table").get(0); // select the first table.
					Elements rows = table.select("tr");
					StringBuilder result = new StringBuilder();

					for (int i = 1; i < 31; i++) {
						Element row = rows.get(i);
						Elements cols = row.select("td");
						String[] data = cols.text().split(" ");
						data[data.length - 3] = "$" + new DecimalFormat("#0.00").format(Math.rint(100.0 * new Double(data[data.length - 3].substring(1))) / 100.0);
						result.append(data[data.length - 3].replace(',', '.') + "  -  ");
						result.append(data[1]);
						int k = 1;

						while (!data[++k].startsWith("XRP")) {
							result.append(" " + data[k]);
						}
						result.append("\n");
					}

					result.append("\ninformation by coinmarketcap.com");
					sendMsg(msg, result.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getBotUsername() {
		return "RipplePrice_bot";
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
