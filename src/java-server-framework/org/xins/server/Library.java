/*
 * $Id: Library.java,v 1.46 2010/10/01 13:23:13 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package org.xins.server;

/**
 * Class that represents the XINS/Java Server Framework library.
 *
 * @version $Revision: 1.46 $ $Date: 2010/10/01 13:23:13 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 *
 * @since XINS 1.0.0
 */
public final class Library {

   /**
    * The version of this library.
    */
   private static final String VERSION = Library.class.getPackage().getImplementationVersion();

   /**
    * Constructs a new <code>Library</code> object.
    */
   private Library() {
      // empty
   }

   /**
    * Returns the name of this library.
    *
    * @return
    *    the name of this library, never <code>null</code>;
    *    for example <code>"XINS/Java Client Framework"</code>.
    *
    * @since XINS 3.0
    */
   public static final String getName() {
      return "XINS/Java Client Framework";
   }

   /**
    * Returns the version of this library.
    *
    * @return
    *    the version of this library, for example <code>"1.0.0"</code>,
    *    never <code>null</code>.
    */
   public static final String getVersion() {
      return VERSION;
   }

   /**
    * Checks if the specified version indicates a production release of XINS.
    *
    * @param version
    *    the XINS version to check, cannot be <code>null</code>.
    *
    * @return
    *    <code>true</code> is the specified XINS version identifies a
    *    production release of XINS, <code>false</code> if it does not.
    *
    * @throws NullPointerException
    *    if <code>version == null</code>.
    */
   static final boolean isProductionRelease(String version) throws NullPointerException {
      return version != null && version.matches("[1-9][0-9]*\\.[0-9]+(\\.[0-9]+)?");
   }

   /**
    * Prints the name and version of this library.
    *
    * @param args
    *    the command line arguments; will be ignored.
    *
    * @since XINS 3.0
    */
   public static final void main(String[] args) {
      System.out.println(getName() + " " + getVersion());
   }
}
