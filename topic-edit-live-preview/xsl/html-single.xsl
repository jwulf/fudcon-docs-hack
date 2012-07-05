<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
				xmlns:exsl="http://exslt.org/common"
				version="1.0"
				exclude-result-prefixes="exsl">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/docbook.xsl"/>
<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/xhtml/titlepage.xsl"/>
<xsl:include href="defaults.xsl"/>
<xsl:include href="xhtml-common.xsl"/>

<xsl:output method="xml" encoding="UTF-8" indent="no" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" omit-xml-declaration="no" />

<xsl:param name="html.append"/>

<!--
From: xhtml/docbook.xsl
Reason: add TOC div for web site
Version:
-->

<xsl:template match="*" mode="process.root">
  <xsl:variable name="doc" select="self::*"/>

  <xsl:call-template name="user.preroot"/>
  <xsl:call-template name="root.messages"/>

  <html>
    <head>
      <xsl:call-template name="system.head.content">
        <xsl:with-param name="node" select="$doc"/>
      </xsl:call-template>
      <xsl:call-template name="head.content">
        <xsl:with-param name="node" select="$doc"/>
      </xsl:call-template>
      <xsl:call-template name="user.head.content">
        <xsl:with-param name="node" select="$doc"/>
      </xsl:call-template>
    </head>
    <body>
      <xsl:call-template name="body.attributes"/>
     <xsl:call-template name="user.header.content">
        <xsl:with-param name="node" select="$doc"/>
      </xsl:call-template>
      <xsl:if test="$embedtoc != 0">
      <div id="tocdiv" class="toc">
       <iframe><xsl:attribute name="id">tocframe</xsl:attribute><xsl:attribute name="class">toc</xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$tocpath"/>/toc.html</xsl:attribute>This is an iframe, to view it upgrade your browser or enable iframe display.</iframe>
      </div>
      </xsl:if>
      <p xmlns="http://www.w3.org/1999/xhtml">
        <xsl:attribute name="id">
           <xsl:text>title</xsl:text>
        </xsl:attribute>
        <!-- <a class="left">
          <xsl:attribute name="href">
              <xsl:value-of select="$prod.url"/>
          </xsl:attribute>
          <img src="Common_Content/images/image_left.png" alt="Product Site"/>
        </a>
        <a class="right">
          <xsl:attribute name="href">
            <xsl:value-of select="$doc.url"/>
          </xsl:attribute>
          <img src="Common_Content/images/image_right.png" alt="Documentation Site"/>
        </a> -->
      </p>
      <xsl:apply-templates select="."/>
      <xsl:call-template name="user.footer.content">
        <xsl:with-param name="node" select="$doc"/>
      </xsl:call-template>
    </body>
  </html>
  <xsl:value-of select="$html.append"/>
</xsl:template>

</xsl:stylesheet>
