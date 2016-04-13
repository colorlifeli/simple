package me.net;

public class NetType {

	// 数据来源有哪些
	public static enum eStockSource {
		SINA("sina"), YAHOO("yahoo");

		private eStockSource(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		public String fieldName() {
			switch (this) {
			case SINA:
				return "code_sina";
			case YAHOO:
				return "code_yahoo";
			default:
				return null;
			}
		}

		private String value;
	}

	// 某支 stock 的状态
	public static enum eStockCodeFlag {
		STOP("01"), ERROR("99");

		private eStockCodeFlag(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		private String value;
	}

	// 分型：顶分型；底分型
	public static enum eStockDayFlag {
		TOP("01"), BOTTOM("02");

		private eStockDayFlag(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		private String value;
	}

	// 操作类型
	public static enum eStockOper {
		Buy("1"), Sell("2"), None("0");

		private eStockOper(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		public static eStockOper from(String value) {
			for (eStockOper v : values())
				if (v.value.equalsIgnoreCase(value))
					return v;
			throw new IllegalArgumentException();
		}

		private String value;
	}

	// 操作策略
	public static enum eStrategy {
		One, // 每次按推荐操作一单位
		OneBuyOneSell, // 严格按照：买一单位后必然卖一单位
		Double;// 符合某些条件则买入（或卖出）更多
	}

}
