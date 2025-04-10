package com.framepayments.framesdk.customers
import com.framepayments.framesdk.FrameNetworking

class CustomersAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomer(request: CustomersRequests.CreateCustomerRequest): Customer? {
        val endpoint = CustomersEndpoints.CreateCustomer
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        val (data, _) = FrameNetworking.performDataTask(endpoint, requestBody)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    Customer::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest): Customer? {
        val endpoint = CustomersEndpoints.UpdateCustomer(customerId)
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        val (data, _) = FrameNetworking.performDataTask(endpoint, requestBody)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    Customer::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun getCustomers(page: Int? = null, perPage: Int? = null): List<Customer>? {
        val endpoint = CustomersEndpoints.GetCustomers(perPage = perPage, page = page)

        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    CustomersResponses.ListCustomersResponse::class.java
                )
                response.data
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun getCustomerWith(customerId: String): Customer? {
        val endpoint = CustomersEndpoints.GetCustomerWith(customerId)

        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    Customer::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest): List<Customer>? {
        val endpoint = CustomersEndpoints.SearchCustomers
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        val (data, _) = FrameNetworking.performDataTask(endpoint, requestBody)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    CustomersResponses.ListCustomersResponse::class.java
                )
                response.data
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun deleteCustomer(customerId: String): Customer? {
        val endpoint = CustomersEndpoints.DeleteCustomer(customerId)

        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return try {
                val jsonString = String(data, Charsets.UTF_8)
                val response = FrameNetworking.gson.fromJson(
                    jsonString,
                    Customer::class.java
                )
                response
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createCustomer(request: CustomersRequests.CreateCustomerRequest, completionHandler: (Customer?) -> Unit) {
        val endpoint = CustomersEndpoints.CreateCustomer
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        FrameNetworking.performDataTask(endpoint, requestBody) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        Customer::class.java
                    )
                    completionHandler(decodedResponse)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }

    fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest, completionHandler: (Customer?) -> Unit) {
        val endpoint = CustomersEndpoints.UpdateCustomer(customerId)
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        FrameNetworking.performDataTask(endpoint, requestBody) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        Customer::class.java
                    )
                    completionHandler(decodedResponse)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }

    fun getCustomers(page: Int? = null, perPage: Int? = null, completionHandler: (List<Customer>?) -> Unit) {
        val endpoint = CustomersEndpoints.GetCustomers(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        CustomersResponses.ListCustomersResponse::class.java
                    )
                    completionHandler(decodedResponse.data)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }

    fun getCustomerWith(customerId: String, completionHandler: (Customer?) -> Unit) {
        val endpoint = CustomersEndpoints.GetCustomerWith(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        Customer::class.java
                    )
                    completionHandler(decodedResponse)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest, completionHandler: (List<Customer>?) -> Unit) {
        val endpoint = CustomersEndpoints.SearchCustomers
        val requestBody: ByteArray? = try {
            FrameNetworking.gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        CustomersResponses.ListCustomersResponse::class.java
                    )
                    completionHandler(decodedResponse.data)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }

    fun deleteCustomer(customerId: String, completionHandler: (Customer?) -> Unit) {
        val endpoint = CustomersEndpoints.DeleteCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                try {
                    val jsonString = String(data, Charsets.UTF_8)
                    val decodedResponse = FrameNetworking.gson.fromJson(
                        jsonString,
                        Customer::class.java
                    )
                    completionHandler(decodedResponse)
                } catch (e: Exception) {
                    completionHandler(null)
                }
            } else {
                completionHandler(null)
            }
        }
    }
}
