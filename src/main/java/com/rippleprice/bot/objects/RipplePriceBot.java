package com.rippleprice.bot.objects;

import java.io.IOException;
import java.text.DecimalFormat;
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

	public void onUpdateReceived(Update arg0) {

		Locale.setDefault(Locale.ENGLISH);
		Message msg = arg0.getMessage();
		StringBuilder result = new StringBuilder();
		Document doc;
		JSONArray jsonArray;
		String command = new String();

		if (msg != null && msg.hasText()) {
		    switch (msg.getText()) {
                case "/start" :
                    sendMsg(msg, String.format(
                        "Hello, *%s*!\n*Available commands:*\n/price - current price of a Ripple\n/markets - Markets price\nhistory - Weekly history\n/top10 - Top 10 cryptocurrency market capitalizations\n/help - Available commands",
                        msg.getFrom().getFirstName()));
                    command = "/start";
                    break;

                case "/help" :
                    sendMsg(msg, "/price - current price of a Ripple\n/markets - Markets price\n/history - Weekly history\n/top10 - Top 10 cryptocurrency market capitalizations\n/help - Available commands");
                    command = "/help";
                    break;

                case "/price" :
                case "/price@RipplePrice_bot" :
                    try {
                    jsonArray = JsonReader.readJsonFromUrl("https://api.coinmarketcap.com/v1/ticker/ripple/");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    result.append(String.format("Current price: *%s*\nChange(24h): %s%%\n———————————\nMarket cap: \n%s\nRank: %s\n———————————\nVolume(24h):\n%s\nСhange(7d): %s%%", new DecimalFormat("$#0.00").format(jsonObject.getDouble("price_usd")),
                            jsonObject.getString("percent_change_24h").startsWith("-") ? jsonObject.getString("percent_change_24h") : ("+" + jsonObject.getString("percent_change_24h")),
                            new DecimalFormat("$###,###.###").format(jsonObject.getDouble("market_cap_usd")), jsonObject.getString("rank"), new DecimalFormat("$###,###.###").format(jsonObject.getDouble("24h_volume_usd")),
                            jsonObject.getString("percent_change_7d").startsWith("-") ? jsonObject.getString("percent_change_7d") : ("+" + jsonObject.getString("percent_change_7d"))));

                    result.append("\n\ninformation by coinmarketcap.com");
                    sendMsg(msg, result.toString());
                    command = "/price";
                } catch (IOException e) {
                    sendMeMsg(String.format("*The exception was intercepted:* %s", e.getStackTrace().toString()));
                }
                    break;

                case "/history" :
                case "/history@RipplePrice_bot" :
                    try {
                        doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/historical-data/").get();
                        Element table = doc.select("table").get(0);
                        Elements rows = table.select("tr");

                        for (int i = 1; i < 8; i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");
                            String[] data = cols.text().split(" ");
                            result.append(data[0] + " " + data[1] + " " + data[2] + "\n" + "High:\n*$" + data[4] + "*" + "\nLow:\n*$" + data[5] + "*" + "\nMarket cap:\n$" + data[8]);
                            if (i < 7)
                                result.append("\n———————————\n");

                        }
                        result.append("\n\ninformation by coinmarketcap.com");
                        sendMsg(msg, result.toString());
                        command = "/history";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "/markets" :
                case "/markets@RipplePrice_bot" :
                    try {
                        doc = Jsoup.connect("https://coinmarketcap.com/currencies/ripple/#markets").get();
                        Element table = doc.select("table").get(0);
                        Elements rows = table.select("tr");

                        for (int i = 1; i < 31; i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");
                            String[] data = cols.text().split(" ");

                            if (data[data.length - 1].equals("Recently")) {
                                try {
                                    data[data.length - 5] = "$" + new DecimalFormat("#0.00").format(Math.rint(100.0 * new Double(data[data.length - 5].substring(1))) / 100.0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                result.append("*").append(data[data.length - 5].replace(',', '.')).append("*").append("  -  ");
                                result.append(data[1]);
                                int k = 1;

                                while (!data[++k].startsWith("XRP")) {
                                    result.append(" ").append(data[k]);
                                }

                                if (data[data.length - 5].startsWith("XRP"))
                                    result.append("  (").append(data[data.length - 6]).append(")");
                                else
                                    result.append("  (").append(data[data.length - 7]).append(")");
                                result.append("\n");
                            }
                        }

                        result.append("\ninformation by coinmarketcap.com");
                        sendMsg(msg, result.toString());
                        command = "/markets";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "/top10" :
                case "/top10@RipplePrice_bot" :
                    try {
                        jsonArray = JsonReader.readJsonFromUrl("https://api.coinmarketcap.com/v1/ticker/?limit=10");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            result.append(String.format("%s. *%s* (%s)\nPrice: *$%s*\nMarket Cap:\n%s\nChange(24h):\n%s%%", jsonArray.getJSONObject(i).get("rank"), jsonArray.getJSONObject(i).get("name"), jsonArray.getJSONObject(i).get("symbol"),
                                    new DecimalFormat("#0.00").format(jsonArray.getJSONObject(i).getDouble("price_usd")).replace(',', '.'), new DecimalFormat("$###,###.###").format(jsonArray.getJSONObject(i).getDouble("market_cap_usd")),
                                    jsonArray.getJSONObject(i).get("percent_change_24h").toString().startsWith("-") ? jsonArray.getJSONObject(i).get("percent_change_24h") : ("+" + jsonArray.getJSONObject(i).get("percent_change_24h"))));
                            if (i < jsonArray.length() - 1)
                                result.append("\n———————————\n");
                        }

                        result.append("\n\ninformation by coinmarketcap.com");
                        sendMsg(msg, result.toString());
                        command = "/top10";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default: sendMeMsg(String.format("The bot was used: *%s %s* (*%s*) - \"%s\"", msg.getFrom().getFirstName(), msg.getFrom().getLastName(), msg.getFrom().getUserName(), msg.getText()));
            }
		}

        if (!msg.getFrom().getId().equals(117079036))
			sendMeMsg(String.format("The bot was used: *%s %s* (*%s*) *%s* - \"%s\"", msg.getFrom().getFirstName(), msg.getFrom().getLastName(), msg.getFrom().getUserName(), msg.getFrom().getLanguageCode(), command));
	}

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    private void sendMsg(Message msg, String text) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(msg.getChatId().toString());
		sendMessage.setReplyToMessageId(msg.getMessageId());
		sendMessage.setText(text);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void sendMeMsg(String text) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId("117079036");
		sendMessage.setText(text);
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public static void start() {
		ApiContextInitializer.init();
		TelegramBotsApi botapi = new TelegramBotsApi();
		try {
			botapi.registerBot(new RipplePriceBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
