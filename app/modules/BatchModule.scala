package modules

import batch.BatchStarter
import play.api.inject._

class BatchModule extends SimpleModule(bind[BatchStarter].toSelf.eagerly())
