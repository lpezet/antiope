/**
 * 
 */
package com.github.lpezet.antiope.samples.yahoo;

import javax.xml.stream.events.XMLEvent;

import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.IntegerStaxUnmarshaller;
import com.github.lpezet.antiope.transform.SimpleTypeStaxUnmarshallers.StringStaxUnmarshaller;
import com.github.lpezet.antiope.transform.StaxUnmarshallerContext;
import com.github.lpezet.antiope.transform.Unmarshaller;

/**
 * @author luc
 *
 */
public class ConditionsUnmarshaller implements Unmarshaller<Conditions, StaxUnmarshallerContext> {
	
	@Override
	public Conditions unmarshall(StaxUnmarshallerContext context) throws Exception {
		Conditions oResult = new Conditions();
		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return oResult;

			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("text")) {
					oResult.setText( StringStaxUnmarshaller.getInstance().unmarshall(context));
				} else if (context.testExpression("code")) {
					oResult.setCode( IntegerStaxUnmarshaller.getInstance().unmarshall(context));
				} else if (context.testExpression("temp")) {
					oResult.setTemperature( IntegerStaxUnmarshaller.getInstance().unmarshall(context));
				} else if (context.testExpression("date")) {
					oResult.setDate( StringStaxUnmarshaller.getInstance().unmarshall(context));
				}
			} else if (xmlEvent.isEndElement()) {
				return oResult;
			}
		}
	}

	private static ConditionsUnmarshaller	instance;

	public static ConditionsUnmarshaller getInstance() {
		if (instance == null) instance = new ConditionsUnmarshaller();
		return instance;
	}
}
