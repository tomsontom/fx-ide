define("orion/editor/javaContentAssist", [], function() {
	function JavaContentAssistProvider() {}
	
	JavaContentAssistProvider.prototype =
	{
		computeProposals: function(buffer, offset, context) {
			var jProps = this.__javaObject.getProposals(context.line, context.prefix, offset);
			var proposals = JSON.parse(jProps);
			// proposals.push( { proposal : "err", description: jProps + " - err - Error Stream" } );
			
			return proposals;
		}
	}
	
	return {
		JavaContentAssistProvider: JavaContentAssistProvider
	};
});