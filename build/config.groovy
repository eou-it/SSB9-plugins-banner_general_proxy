
withConfig(configuration) {
    inline(phase: 'CONVERSION') { source, context, classNode ->
        classNode.putNodeMetaData('projectVersion', '1.0')
        classNode.putNodeMetaData('projectName', 'banner_general_proxy')
        classNode.putNodeMetaData('isPlugin', 'true')
    }
}
