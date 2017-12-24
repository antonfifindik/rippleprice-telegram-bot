package com.rippleprice.bot.objects;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
				sendMsg(msg, "/price - current price of a Ripple");
			if (msg.getText().equals("/price")) {
				try {
					Document doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/").get();
					String[] marketCap = doc.select(".coin-summary-item-detail.details-text-medium>span").text().split(" ");
					String[] priceChange = doc.select(".text-large2").text().split(" ");
					sendMsg(msg, "Current price: " + doc.select("span#quote_price").text() + "\n" + "Price change: " + priceChange[1] + "\n" + "—————————————" + "\n" + "Market Cap: " + "\n" + marketCap[0] + " " + marketCap[1] + "\n" + marketCap[2]
							+ " " + marketCap[3] + "\n" + "—————————————" + "\n" + "Volume (24h): " + "\n" + marketCap[4] + " " + marketCap[5] + "\n" + marketCap[6] + " " + marketCap[7] + "\n\n" + "information by coinmarketcap.com");

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
