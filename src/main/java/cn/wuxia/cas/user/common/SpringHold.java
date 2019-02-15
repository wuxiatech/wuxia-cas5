package cn.wuxia.cas.user.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringHold {
	
	private static ApplicationContext ctx=null;
	
	public static ApplicationContext getSpringContext(){
		if(ctx==null){

			ctx = new ClassPathXmlApplicationContext("classpath:/spring-configuration/applicationContext.xml");
			return ctx;
		}else{
			return ctx;
		}
		
	}

}
