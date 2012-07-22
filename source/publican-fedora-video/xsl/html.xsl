<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		version='1.0'
		xmlns="http://www.w3.org/TR/xhtml1/transitional"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		exclude-result-prefixes="#default">

<xsl:import href="http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl"/>
<xsl:import href="../../../xsl/html.xsl"/>

<!-- inject our javascript into the head -->
<!-- http://www.sagehill.net/docbookxsl/InsertExtHtml.html -->
<!-- Got this async lazy loader from
  http://friendlybit.com/js/lazy-loading-asyncronous-javascript/ -->
<xsl:template name="user.head.content">
   <script type="text/javascript">
(function() {
    function async_load(){
        var s = document.createElement('script');
        s.type = 'text/javascript';
        s.async = true;
        s.src = 'Common_Content/css/code-lang-switcher.js';
        var x = document.getElementsByTagName('script')[0];
        x.parentNode.insertBefore(s, x);
    }
    if (window.attachEvent)
        window.attachEvent('onload', async_load);
    else
        window.addEventListener('load', async_load, false);
})();</script>
</xsl:template>

<!-- set the onload of the page -->
<!-- http://www.sagehill.net/docbookxsl/BodyAtts.html -->
<!-- <xsl:template name="body.attributes">
  <xsl:attribute name="onLoad">skynetBookLoad()</xsl:attribute>
</xsl:template> -->

<xsl:template match="videoobject">
  <xsl:apply-templates select="videodata"/>
</xsl:template>

<xsl:template match="videodata">
  <xsl:call-template name="process.image">
    <xsl:with-param name="tag" select="'iframe'"/>
    <xsl:with-param name="alt">
      <xsl:apply-templates select="(../../textobject/phrase)[1]"/>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:param name="local.l10n.xml" select="document('')"/>
<l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
  <l:l10n language="en">
    <l:context name="xref-number-and-title">
      <l:template name="section" style="simpletarget" text="%t"/>
    </l:context>
  </l:l10n>
</l:i18n>

<xsl:param name="local.l10n.xml" select="document('')"/>
<l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
  <l:l10n language="en">
    <l:context name="xref-number-and-title">
      <l:template name="chapter" style="simpletarget" text="%t"/>
    </l:context>
  </l:l10n>
</l:i18n>


<xsl:param name="generate.toc">
set toc
book toc
article toc
chapter toc
qandadiv toc
qandaset toc
sect1 nop
sect2 nop
sect3 nop
sect4 nop
sect5 nop
section toc
part toc
</xsl:param>
</xsl:stylesheet>

