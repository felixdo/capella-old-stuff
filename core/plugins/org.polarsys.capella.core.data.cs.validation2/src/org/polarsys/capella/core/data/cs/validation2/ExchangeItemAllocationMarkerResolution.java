package org.polarsys.capella.core.data.cs.validation2;

public class ExchangeItemAllocationMarkerResolution extends CommandMarkerResolutionGenerator {

  public ExchangeItemAllocationMarkerResolution() {
    super(new ExchangeItemAllocationResolver(true));
  }

}
