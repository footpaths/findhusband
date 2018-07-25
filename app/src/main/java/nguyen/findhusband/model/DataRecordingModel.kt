package nguyen.findhusband.model

class DataRecordingModel {

    var idParams: String?=null
    var contentVideo: String?=null


    constructor() {}
    constructor(idParams: String,contentVideo: String ) {
        this.idParams = idParams
        this.contentVideo = contentVideo


    }


    override fun toString(): String {
        return "$idParams $contentVideo"
    }
    //fsdfsd
}