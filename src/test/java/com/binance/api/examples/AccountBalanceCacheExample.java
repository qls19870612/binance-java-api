package com.binance.api.examples;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

import static com.binance.api.client.domain.event.UserDataUpdateEvent.UserDataUpdateEventType.ACCOUNT_UPDATE;

/**
 * Illustrates how to use the user data event stream to create a local cache for the balance of an account.
 */
public class AccountBalanceCacheExample {
  private static final Logger logger = LoggerFactory.getLogger(AccountBalanceCacheExample.class);
  private final BinanceApiClientFactory clientFactory;

  /**
   * Key is the symbol, and the value is the balance of that symbol on the account.
   */
  private Map<String, AssetBalance> accountBalanceCache;

  /**
   * Listen key used to interact with the user data streaming API.
   */
  private final String listenKey;

  public AccountBalanceCacheExample(String apiKey, String secret) {
    this.clientFactory = BinanceApiClientFactory.newInstance(apiKey, secret);
    this.listenKey = initializeAssetBalanceCacheAndStreamSession();
    startAccountBalanceEventStreaming(listenKey);
  }

  /**
   * Initializes the asset balance cache by using the REST API and starts a new user data streaming session.
   *
   * @return a listenKey that can be used with the user data streaming API.
   */
  private String initializeAssetBalanceCacheAndStreamSession() {
    BinanceApiRestClient client = clientFactory.newRestClient();
    Account account = client.getAccount();

    this.accountBalanceCache = new TreeMap<>();
    for (AssetBalance assetBalance : account.getBalances()) {
      double v = Double.parseDouble(assetBalance.getFree());
      if (v > 0) {
        logger.debug("initializeAssetBalanceCacheAndStreamSession assetBalance:{},v:{}", assetBalance,v);
      }
      accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
    }

    return client.startUserDataStream();
  }

  /**
   * Begins streaming of agg trades events.
   */
  private void startAccountBalanceEventStreaming(String listenKey) {
    BinanceApiWebSocketClient client = clientFactory.newWebSocketClient();

    client.onUserDataUpdateEvent(listenKey, response -> {
      if (response.getEventType() == ACCOUNT_UPDATE) {
        // Override cached asset balances
        for (AssetBalance assetBalance : response.getAccountUpdateEvent().getBalances()) {
          accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
          double v = Double.parseDouble(assetBalance.getAsset());
          if (v > 0) {
            logger.debug("startAccountBalanceEventStreaming assetBalance:{}", assetBalance);
          }
        }
        System.out.println(accountBalanceCache);
      }
    });
  }

  /**
   * @return an account balance cache, containing the balance for every asset in this account.
   */
  public Map<String, AssetBalance> getAccountBalanceCache() {
    return accountBalanceCache;
  }

  public static void main(String[] args) {
    new AccountBalanceCacheExample("YOUR_API_KEY", "YOUR_SECRET");
  }
}
