package common;

import web.example.HelloAction;

public class ActionFactory {
	
	public static class holder {
		private static final ActionFactory instance = new ActionFactory();
	}
	
	private ActionFactory() {}
	
	public static final ActionFactory me() {
		return holder.instance;
	}
	
	ActionIf getAction(String name) {
		//Action action;
		
		return new HelloAction();
	}
}
