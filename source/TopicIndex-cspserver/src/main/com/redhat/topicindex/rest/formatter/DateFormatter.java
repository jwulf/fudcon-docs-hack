package com.redhat.topicindex.rest.formatter;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.resteasy.spi.StringParameterUnmarshaller;
import org.jboss.resteasy.util.FindAnnotation;

public class DateFormatter implements StringParameterUnmarshaller<Date>
{
   private SimpleDateFormat formatter;

   @Override
   public void setAnnotations(final Annotation[] annotations)
   {
      final DateFormat format = FindAnnotation.findAnnotation(annotations, DateFormat.class);
      formatter = new SimpleDateFormat(format.value());
   }

   @Override
   public Date fromString(final String str)
   {
      try
      {
         return formatter.parse(str);
      }
      catch (final ParseException ex)
      {
         throw new RuntimeException(ex);
      }
   }

}
