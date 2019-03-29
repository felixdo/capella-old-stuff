package org.polarsys.capella.core.data.cs.validation2.tests.common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class StatusMatcher {

  public static Matcher<IStatus> okStatus(){
    return new TypeSafeMatcher<IStatus>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(Status.OK_STATUS.toString());
      }
  
      @Override
      protected boolean matchesSafely(IStatus item) {
        return item.isOK();
      }
    };
  }

  public static FeatureMatcher<IStatus, Integer> severity(Matcher<? super Integer> matcher){
    return new FeatureMatcher<IStatus, Integer>(matcher, "validation result severity", "severity") {
      @Override
      protected Integer featureValueOf(IStatus actual) {
        return actual.getSeverity();
      }
    };
  }

}
