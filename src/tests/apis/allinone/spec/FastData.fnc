<?xml version="1.0" encoding="US-ASCII"?>
<!DOCTYPE function PUBLIC "-//XINS//DTD Function 3.1//EN" "http://xins.sourceforge.net/dtd/function_3_1.dtd">

<function name="FastData" cache="60">

	<description>Copies the input to the output.</description>

	<input>
		<param name="productId" required="true" type="_int64">
			<description>The product ID.</description>
		</param>
		<param name="productDataClientTimestamp" required="false" type="_int64">
			<description>The timestamp of the product information currently on the client side.</description>
		</param>
	</input>

	<output>
		<resultcode-ref name="InvalidNumber" />
		<param name="productDescription" required="true">
			<description>The description of the product.</description>
		</param>
		<param name="productDataTimestamp" required="true" type="_int64">
			<description>The timestamp of the data.</description>
		</param>
	</output>

	<example>
		<description>Gets information about a product</description>
		<input-example name="productId">123456</input-example>
		<output-example name="productDescription">A table.</output-example>
		<output-example name="productDataTimestamp">654321123456</output-example>
	</example>
</function>
