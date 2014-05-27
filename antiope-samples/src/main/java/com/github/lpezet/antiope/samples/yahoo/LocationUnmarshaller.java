package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.StringStaxUnmarshaller;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

class LocationUnmarshaller implements Unmarshaller<Location, StaxUnmarshallerContext> {
	@Override
	public Location unmarshall(StaxUnmarshallerContext context) throws Exception {
		Location oResult = new Location();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("city")) {
					oResult.setCity(StringStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				}
				if (context.testExpression("region")) {
					oResult.setRegion(StringStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				}
				if (context.testExpression("country")) {
					oResult.setCountry(StringStaxUnmarshaller.getInstance().unmarshall(context));
					continue;
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static LocationUnmarshaller	instance;

	public static LocationUnmarshaller getInstance() {
		if (instance == null) instance = new LocationUnmarshaller();
		return instance;
	}
}