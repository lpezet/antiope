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
public class ForecastUnmarshaller implements Unmarshaller<Forecast, StaxUnmarshallerContext> {

	public Forecast unmarshall(StaxUnmarshallerContext context) throws Exception {
		Forecast f = new Forecast();

		while (true) {
			XMLEvent xmlEvent = context.nextEvent();
			if (xmlEvent.isEndDocument()) return f;
			if (xmlEvent.isAttribute() || xmlEvent.isStartElement()) {
				if (context.testExpression("day")) {
					f.setDay( StringStaxUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("date")) {
					f.setDate( StringStaxUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("low")) {
					f.setLow( IntegerStaxUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("high")) {
					f.setHigh( IntegerStaxUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("text")) {
					f.setText( StringStaxUnmarshaller.getInstance().unmarshall(context) );
				} else if (context.testExpression("code")) {
					f.setCode( IntegerStaxUnmarshaller.getInstance().unmarshall(context) );
				}
			} else if (xmlEvent.isEndElement()) {
				return f;
			}
		}
	}

	private static ForecastUnmarshaller	instance;

	public static ForecastUnmarshaller getInstance() {
		if (instance == null) instance = new ForecastUnmarshaller();
		return instance;
	}

}
