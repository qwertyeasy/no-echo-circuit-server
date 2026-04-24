package com.qwertyeasy.repository

import com.qwertyeasy.data.entity.AddNotification
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : CrudRepository<AddNotification, String>