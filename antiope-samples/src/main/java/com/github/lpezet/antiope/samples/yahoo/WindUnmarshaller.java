package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.DoubleStaxUnmarshaller;
import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.IntegerStaxUnmarshaller;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

class WindUnmarshaller implements Unmarshaller<Wind, StaxUnmarshallerContext> {
	@Override
	public Wind unmarshall(StaxUnmarshallerContext context) throws Exception {
		Wind oResult = new Wind();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("chill")) {
					oResult.setChill(DoubleStaxUnmarshaller.getInstance().unmarshall(context));
				} else if (context.testExpression("direction")) {
					oResult.setDirection(IntegerStaxUnmarshaller.getInstance().unmarshall(context));
				} else if (context.testExpression("speed")) {
					oResult.setSpeed(DoubleStaxUnmarshaller.getInstance().unmarshall(context));
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static WindUnmarshaller	instance;

	public static WindUnmarshaller getInstance() {
		if (instance == null) instance = new WindUnmarshaller();
		return instance;
	}
}